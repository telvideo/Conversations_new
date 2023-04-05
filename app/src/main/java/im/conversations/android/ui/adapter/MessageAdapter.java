/*
 * Copyright (c) 2023, Daniel Gultsch
 *
 * This file is part of Conversations.
 *
 * Conversations is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Conversations is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Conversations.  If not, see <https://www.gnu.org/licenses/>.
 */

package im.conversations.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import im.conversations.android.R;
import im.conversations.android.database.model.MessageAdapterItem;
import im.conversations.android.database.model.MessageWithContentReactions;
import im.conversations.android.databinding.ItemMessageReceivedBinding;
import im.conversations.android.databinding.ItemMessageSentBinding;
import im.conversations.android.databinding.ItemMessageSeparatorBinding;
import im.conversations.android.ui.AvatarFetcher;
import im.conversations.android.ui.graphics.drawable.FlashBackgroundDrawable;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageAdapter
        extends PagingDataAdapter<MessageAdapterItem, MessageAdapter.AbstractMessageViewHolder> {

    private static final int VIEW_TYPE_RECEIVED = 0;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_SEPARATOR = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAdapter.class);

    private Consumer<Long> onNavigateToInReplyTo;

    public MessageAdapter(@NonNull DiffUtil.ItemCallback<MessageAdapterItem> diffCallback) {
        super(diffCallback);
    }

    @Override
    public int getItemViewType(final int position) {
        final var item = peek(position);
        if (item instanceof MessageAdapterItem.MessageDateSeparator) {
            return VIEW_TYPE_SEPARATOR;
        } else if (item instanceof MessageWithContentReactions m) {
            return m.outgoing ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public AbstractMessageViewHolder onCreateViewHolder(
            final @NonNull ViewGroup parent, final int viewType) {
        final var layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_RECEIVED) {
            return new MessageReceivedViewHolder(
                    DataBindingUtil.inflate(
                            layoutInflater, R.layout.item_message_received, parent, false));
        } else if (viewType == VIEW_TYPE_SENT) {
            return new MessageSentViewHolder(
                    DataBindingUtil.inflate(
                            layoutInflater, R.layout.item_message_sent, parent, false));
        } else if (viewType == VIEW_TYPE_SEPARATOR) {
            return new MessageDateSeparator(
                    DataBindingUtil.inflate(
                            layoutInflater, R.layout.item_message_separator, parent, false));
        }
        throw new IllegalArgumentException(String.format("viewType %d not implemented", viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMessageViewHolder holder, int position) {
        final var item = getItem(position);
        if (item == null) {
            holder.setItem(null);
        } else if (item instanceof MessageWithContentReactions message) {
            this.onBindViewHolder(holder, message);
        } else if (item instanceof MessageAdapterItem.MessageDateSeparator dateSeparator) {
            this.onBindViewHolder(holder, dateSeparator);
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s is not a known implementation", item.getClass().getSimpleName()));
        }
    }

    private void onBindViewHolder(
            @NonNull final AbstractMessageViewHolder holder,
            @NonNull final MessageAdapterItem.MessageDateSeparator dateSeparator) {
        holder.setItem(dateSeparator);
    }

    private void onBindViewHolder(
            @NonNull AbstractMessageViewHolder holder,
            @NonNull final MessageWithContentReactions message) {
        holder.setItem(message);
        if (holder.itemView.getBackground() instanceof FlashBackgroundDrawable backgroundDrawable) {
            if (backgroundDrawable.needsReset(message.id)) {
                holder.itemView.setBackground(null);
            }
        }
        final var inReplyTo = message.inReplyTo;
        if (holder instanceof MessageReceivedViewHolder messageReceivedViewHolder) {
            if (inReplyTo != null) {
                messageReceivedViewHolder.binding.embeddedMessage.setOnClickListener(
                        view -> onNavigateToInReplyTo.accept(inReplyTo.id));
            }
            final var addressWithName = message.getAddressWithName();
            final var avatar = message.getAvatar();
            if (avatar != null) {
                AvatarFetcher.fetchInto(messageReceivedViewHolder.binding.avatar, avatar);
            } else {
                AvatarFetcher.setDefault(messageReceivedViewHolder.binding.avatar, addressWithName);
            }
        } else if (holder instanceof MessageSentViewHolder messageSentViewHolder) {
            if (inReplyTo != null) {
                messageSentViewHolder.binding.embeddedMessage.setOnClickListener(
                        view -> onNavigateToInReplyTo.accept(inReplyTo.id));
            }
        }
    }

    public void setOnNavigateToInReplyTo(final Consumer<Long> consumer) {
        this.onNavigateToInReplyTo = consumer;
    }

    protected abstract static class AbstractMessageViewHolder extends RecyclerView.ViewHolder {

        private AbstractMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract void setItem(final MessageAdapterItem item);
    }

    public static class MessageReceivedViewHolder extends AbstractMessageViewHolder {

        private final ItemMessageReceivedBinding binding;

        public MessageReceivedViewHolder(@NonNull ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected void setItem(final MessageAdapterItem item) {
            if (item instanceof MessageWithContentReactions message) {
                this.binding.setMessage(message);
                this.binding.content.setClipToOutline(true);
            } else {
                this.binding.setMessage(null);
            }
        }
    }

    public static class MessageSentViewHolder extends AbstractMessageViewHolder {

        private final ItemMessageSentBinding binding;

        public MessageSentViewHolder(@NonNull ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected void setItem(MessageAdapterItem item) {
            if (item instanceof MessageWithContentReactions message) {
                this.binding.setMessage(message);
            } else {
                this.binding.setMessage(null);
            }
        }
    }

    public static class MessageDateSeparator extends AbstractMessageViewHolder {

        private final ItemMessageSeparatorBinding binding;

        private MessageDateSeparator(@NonNull ItemMessageSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected void setItem(MessageAdapterItem item) {
            if (item instanceof MessageAdapterItem.MessageDateSeparator dateSeparator) {
                this.binding.setTimestamp(dateSeparator.date);
            } else {
                this.binding.setTimestamp(null);
            }
        }
    }
}
