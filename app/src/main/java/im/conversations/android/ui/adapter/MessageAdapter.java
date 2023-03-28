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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageAdapter
        extends PagingDataAdapter<MessageAdapterItem, MessageAdapter.AbstractMessageViewHolder> {

    private static final int VIEW_TYPE_RECEIVED = 0;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_SEPARATOR = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAdapter.class);

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
        if (holder instanceof MessageReceivedViewHolder messageReceivedViewHolder) {
            final var addressWithName = message.getAddressWithName();
            final var avatar = message.getAvatar();
            if (avatar != null) {
                AvatarFetcher.fetchInto(messageReceivedViewHolder.binding.avatar, avatar);
            } else {
                AvatarFetcher.setDefault(messageReceivedViewHolder.binding.avatar, addressWithName);
            }
        }
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
