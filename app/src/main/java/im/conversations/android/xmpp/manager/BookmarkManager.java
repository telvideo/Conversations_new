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

package im.conversations.android.xmpp.manager;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import im.conversations.android.xml.Namespace;
import im.conversations.android.xmpp.NodeConfiguration;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.bookmark.Conference;
import im.conversations.android.xmpp.model.bookmark.Nick;
import im.conversations.android.xmpp.model.pubsub.Items;
import im.conversations.android.xmpp.model.pubsub.event.Retract;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookmarkManager extends AbstractManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkManager.class);

    public BookmarkManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void fetch() {
        final var future = getManager(PepManager.class).fetchItems(Conference.class);
        Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(final Map<String, Conference> bookmarks) {
                        setBookmarks(bookmarks);
                    }

                    @Override
                    public void onFailure(@NonNull final Throwable throwable) {
                        LOGGER.warn("Could not fetch bookmarks", throwable);
                    }
                },
                MoreExecutors.directExecutor());
    }

    private void setBookmarks(final Map<String, Conference> bookmarks) {
        final var database = getDatabase();
        final var account = getAccount();
        database.runInTransaction(
                () -> {
                    database.bookmarkDao().setItems(account, bookmarks);
                    database.chatDao().syncWithBookmarks(account);
                });
        getManager(MultiUserChatManager.class).joinMultiUserChats();
    }

    private void updateItems(final Map<String, Conference> items) {
        getDatabase().bookmarkDao().updateItems(getAccount(), items);
    }

    private void deleteItems(Collection<Retract> retractions) {
        final Collection<Jid> addresses =
                Collections2.transform(retractions, r -> JidCreate.fromOrNull(r.getId()));
        getDatabase()
                .bookmarkDao()
                .delete(getAccount().id, Collections2.filter(addresses, Objects::nonNull));
    }

    public void deleteAllItems() {
        getDatabase().bookmarkDao().deleteAll(getAccount().id);
    }

    public void handleItems(final Items items) {
        final var retractions = items.getRetractions();
        final var itemMap = items.getItemMap(Conference.class);
        if (retractions.size() > 0) {
            deleteItems(retractions);
        }
        if (itemMap.size() > 0) {
            updateItems(itemMap);
        }
    }

    public ListenableFuture<Void> publishBookmark(final BareJid address, final boolean autoJoin) {
        return publishBookmark(address, autoJoin, null);
    }

    public ListenableFuture<Void> publishBookmark(
            final Jid address, final boolean autoJoin, final String nick) {
        final var itemId = address.toString();
        final var conference = new Conference();
        conference.setAutoJoin(autoJoin);
        if (nick != null) {
            conference.addExtension(new Nick()).setContent(nick);
        }
        return Futures.transform(
                getManager(PepManager.class)
                        .publish(conference, itemId, NodeConfiguration.WHITELIST_MAX_ITEMS),
                result -> null,
                MoreExecutors.directExecutor());
    }

    public ListenableFuture<Void> retractBookmark(final Jid address) {
        final var itemId = address.toString();
        return Futures.transform(
                getManager(PepManager.class).retract(itemId, Namespace.BOOKMARKS2),
                result -> null,
                MoreExecutors.directExecutor());
    }
}
