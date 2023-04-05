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

package im.conversations.android.xmpp.processor;

import android.content.Context;
import im.conversations.android.xml.Namespace;
import im.conversations.android.xmpp.Entity;
import im.conversations.android.xmpp.Range;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.manager.ArchiveManager;
import im.conversations.android.xmpp.manager.AxolotlManager;
import im.conversations.android.xmpp.manager.BlockingManager;
import im.conversations.android.xmpp.manager.BookmarkManager;
import im.conversations.android.xmpp.manager.DiscoManager;
import im.conversations.android.xmpp.manager.PresenceManager;
import im.conversations.android.xmpp.manager.RosterManager;
import java.util.List;
import java.util.function.Consumer;
import org.jxmpp.jid.Jid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindProcessor extends XmppConnection.Delegate implements Consumer<Jid> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BindProcessor.class);

    public BindProcessor(final Context context, final XmppConnection connection) {
        super(context, connection);
    }

    @Override
    public void accept(final Jid jid) {
        final var account = getAccount();
        final var database = getDatabase();
        final var archive = jid.asBareJid();
        final List<Range> catchUpQueryRanges =
                database.runInTransaction(
                        () -> {
                            database.chatDao().resetMucStates();
                            database.presenceDao().deletePresences(account.id);
                            database.discoDao().deleteUnused(account.id);
                            return database.archiveDao().resetLivePage(account, archive);
                        });

        getManager(RosterManager.class).fetch();

        final var discoManager = getManager(DiscoManager.class);

        if (discoManager.hasServerFeature(Namespace.BLOCKING)) {
            getManager(BlockingManager.class).fetch();
        }

        if (discoManager.hasServerFeature(Namespace.COMMANDS)) {
            discoManager.items(
                    Entity.discoItem(account.address.asDomainBareJid()), Namespace.COMMANDS);
        }

        getManager(BookmarkManager.class).fetch();

        getManager(AxolotlManager.class).publishIfNecessary();

        getManager(ArchiveManager.class).query(archive, catchUpQueryRanges);

        getManager(PresenceManager.class).sendPresence();
    }
}
