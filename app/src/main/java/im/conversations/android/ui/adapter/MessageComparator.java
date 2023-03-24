package im.conversations.android.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import im.conversations.android.database.model.MessageWithContentReactions;

public class MessageComparator extends DiffUtil.ItemCallback<MessageWithContentReactions> {
    @Override
    public boolean areItemsTheSame(
            @NonNull MessageWithContentReactions oldItem,
            @NonNull MessageWithContentReactions newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(
            @NonNull MessageWithContentReactions oldItem,
            @NonNull MessageWithContentReactions newItem) {
        return false;
    }
}
