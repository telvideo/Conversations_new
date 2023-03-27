package im.conversations.android.database.model;

import java.time.Instant;

public sealed interface MessageAdapterItem
        permits MessageAdapterItem.MessageDateSeparator, MessageWithContentReactions {

    final class MessageDateSeparator implements MessageAdapterItem {
        public final Instant date;

        public MessageDateSeparator(Instant date) {
            this.date = date;
        }
    }
}
