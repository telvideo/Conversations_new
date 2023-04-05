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

package im.conversations.android.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.google.common.collect.Collections2;
import im.conversations.android.database.entity.BookmarkEntity;
import im.conversations.android.database.model.Account;
import im.conversations.android.xmpp.model.bookmark.Conference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

@Dao
public abstract class BookmarkDao {

    @Query("DELETE FROM bookmark WHERE accountId=:account")
    public abstract void deleteAll(final long account);

    @Query("DELETE FROM bookmark WHERE accountId=:account and address IN(:addresses)")
    public abstract void delete(final long account, Collection<Jid> addresses);

    @Insert
    protected abstract void insert(Collection<BookmarkEntity> bookmarks);

    @Transaction
    public void updateItems(final Account account, Map<String, Conference> items) {
        final Collection<Jid> addresses =
                Collections2.transform(items.keySet(), JidCreate::fromOrNull);
        delete(account.id, addresses);
        final var entities =
                Collections2.transform(
                        items.entrySet(), entry -> BookmarkEntity.of(account.id, entry));
        // non null filtering is required because BookmarkEntity.of() can return null values if the
        insert(Collections2.filter(entities, Objects::nonNull));
    }

    @Transaction
    public void setItems(Account account, Map<String, Conference> items) {
        deleteAll(account.id);
        final var entities =
                Collections2.transform(
                        items.entrySet(), entry -> BookmarkEntity.of(account.id, entry));
        // non null filtering is required because BookmarkEntity.of() can return null values if the
        insert(Collections2.filter(entities, Objects::nonNull));
    }
}
