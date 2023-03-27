package im.conversations.android.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import im.conversations.android.database.model.MessageAdapterItem;
import im.conversations.android.database.model.MessageWithContentReactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageComparator extends DiffUtil.ItemCallback<MessageAdapterItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageComparator.class);

    @Override
    public boolean areItemsTheSame(
            @NonNull MessageAdapterItem oldItem, @NonNull MessageAdapterItem newItem) {
        if (oldItem instanceof MessageWithContentReactions oldMessage
                && newItem instanceof MessageWithContentReactions newMessage) {
            return oldMessage.id == newMessage.id;
        } else if (oldItem instanceof MessageAdapterItem.MessageDateSeparator oldSeparator
                && newItem instanceof MessageAdapterItem.MessageDateSeparator newSeparator) {
            return oldSeparator.date.equals(newSeparator.date);
        } else {
            return false;
        }
    }

    @Override
    public boolean areContentsTheSame(
            @NonNull MessageAdapterItem oldItem, @NonNull MessageAdapterItem newItem) {
        if (oldItem instanceof MessageWithContentReactions oldMessage
                && newItem instanceof MessageWithContentReactions newMessage) {
            final var areContentsTheSame = oldMessage.equals(newMessage);
            if (!areContentsTheSame) {
                LOGGER.info("Message {} got modified", oldMessage.id);
            }
            return areContentsTheSame;
        } else if (oldItem instanceof MessageAdapterItem.MessageDateSeparator oldSeparator
                && newItem instanceof MessageAdapterItem.MessageDateSeparator newSeparator) {
            return oldSeparator.date.equals(newSeparator.date);
        } else {
            return false;
        }
    }
}
