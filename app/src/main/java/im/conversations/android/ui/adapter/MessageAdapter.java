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
import im.conversations.android.database.model.MessageWithContentReactions;
import im.conversations.android.databinding.ItemMessageReceivedBinding;

public class MessageAdapter extends PagingDataAdapter<MessageWithContentReactions, MessageAdapter.AbstractMessageViewHolder> {

    public MessageAdapter(@NonNull DiffUtil.ItemCallback<MessageWithContentReactions> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public AbstractMessageViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final var layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            return new MessageReceivedViewHolder(DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.item_message_received,
                    parent,
                    false));
        }
        throw new IllegalArgumentException(String.format("viewType %d not implemented", viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMessageViewHolder holder, int position) {
        final var message = getItem(position);
        if (message == null) {
            holder.setMessage(null);
        }
        holder.setMessage(message);
    }

    protected abstract static class AbstractMessageViewHolder extends RecyclerView.ViewHolder {

        private AbstractMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract void setMessage(final MessageWithContentReactions message);
    }

    public static class MessageReceivedViewHolder extends AbstractMessageViewHolder {

        private final ItemMessageReceivedBinding binding;


        public MessageReceivedViewHolder(@NonNull ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected void setMessage(final MessageWithContentReactions message) {
            if (message == null) {
                this.binding.setMessage(null);
                this.binding.text.setText("(placeholder)");
            } else {
                this.binding.setMessage(message);
            }
        }
    }
}
