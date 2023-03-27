package im.conversations.android.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import im.conversations.android.database.model.MessageWithContentReactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageComparator extends DiffUtil.ItemCallback<MessageWithContentReactions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageComparator.class);

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
        final var equals = oldItem.equals(newItem);
        if (!equals) {
            LOGGER.info("Message {} got modified", oldItem.id);
        }
        return false;
    }
}
