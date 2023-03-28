package im.conversations.android.ui.adapter;

import androidx.annotation.Nullable;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import com.google.common.util.concurrent.MoreExecutors;
import im.conversations.android.database.model.MessageAdapterItem;
import im.conversations.android.database.model.MessageWithContentReactions;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public final class MessageAdapterItems {

    private MessageAdapterItems() {}

    public static PagingData<MessageAdapterItem> insertSeparators(
            final PagingData<MessageWithContentReactions> pagingData) {
        return PagingDataTransforms.insertSeparators(
                pagingData,
                MoreExecutors.directExecutor(),
                (before, after) -> {
                    final var dayBefore = zonedDay(before);
                    final var dayAfter = zonedDay(after);
                    if (dayAfter == null && dayBefore != null) {
                        return new MessageAdapterItem.MessageDateSeparator(dayBefore.toInstant());
                    } else if (dayBefore == null || dayBefore.equals(dayAfter)) {
                        return null;
                    } else {
                        return new MessageAdapterItem.MessageDateSeparator(dayBefore.toInstant());
                    }
                });
    }

    private static ZonedDateTime zonedDay(@Nullable final MessageWithContentReactions message) {
        return message == null ? null : zonedDay(message.sentAt);
    }

    private static ZonedDateTime zonedDay(final Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
    }

    public static PagingData<MessageAdapterItem> of(
            final PagingData<MessageWithContentReactions> pagingData) {
        return PagingDataTransforms.map(pagingData, MoreExecutors.directExecutor(), m -> m);
    }
}
