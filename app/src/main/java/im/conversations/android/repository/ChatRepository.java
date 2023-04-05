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

package im.conversations.android.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import com.google.common.util.concurrent.ListenableFuture;
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

    public ListenableFuture<Integer> getMessagePosition(final long chatId, final long messageId) {
        return this.database.messageDao().getPosition(chatId, messageId);
    }
}
