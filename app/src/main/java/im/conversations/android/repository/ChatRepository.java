package im.conversations.android.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import im.conversations.android.database.model.ChatFilter;
import im.conversations.android.database.model.ChatInfo;
import im.conversations.android.database.model.ChatOverviewItem;
import im.conversations.android.database.model.GroupIdentifier;
import im.conversations.android.database.model.MessageWithContentReactions;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRepository extends AbstractRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatRepository.class);

    public ChatRepository(Context context) {
        super(context);
    }

    public LiveData<List<GroupIdentifier>> getGroups() {
        return this.database.chatDao().getGroups();
    }

    public PagingSource<Integer, ChatOverviewItem> getChatOverview(final ChatFilter chatFilter) {
        return this.database.chatDao().getChatOverview(chatFilter);
    }

    public LiveData<ChatInfo> getChatInfo(final long chatId) {
        return this.database.chatDao().getChatInfo(chatId);
    }

    public PagingSource<Integer, MessageWithContentReactions> getMessages(final long chatId) {
        return this.database.messageDao().getMessages(chatId);
    }
}
