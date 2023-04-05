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
import com.google.common.collect.Collections2;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.blocking.Block;
import im.conversations.android.xmpp.model.blocking.Blocklist;
import im.conversations.android.xmpp.model.blocking.Item;
import im.conversations.android.xmpp.model.blocking.Unblock;
import im.conversations.android.xmpp.model.stanza.Iq;
import java.util.Objects;

public class BlockingManager extends AbstractManager {

    public BlockingManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void handlePush(final Block block) {
        final var items = block.getExtensions(Item.class);
        final var addresses =
                Collections2.transform(
                        Collections2.filter(items, i -> Objects.nonNull(i.getJid())), Item::getJid);
        getDatabase().blockingDao().add(getAccount(), addresses);
    }

    public void handlePush(final Unblock unblock) {
        final var items = unblock.getExtensions(Item.class);
        if (items.isEmpty()) {
            getDatabase().blockingDao().clear(getAccount().id);
        } else {
            final var addresses =
                    Collections2.transform(
                            Collections2.filter(items, i -> Objects.nonNull(i.getJid())),
                            Item::getJid);
            getDatabase().blockingDao().remove(getAccount().id, addresses);
        }
    }

    public void fetch() {
        final Iq iqPacket = new Iq(Iq.Type.GET);
        iqPacket.addChild(new Blocklist());
        connection.sendIqPacket(iqPacket, this::handleFetchResult);
    }

    private void handleFetchResult(final Iq result) {
        if (result.getType() != Iq.Type.RESULT) {
            return;
        }
        final var blocklist = result.getExtension(Blocklist.class);
        if (blocklist == null) {
            return;
        }
        final var account = getAccount();
        final var items =
                blocklist.getExtensions(im.conversations.android.xmpp.model.blocking.Item.class);
        final var filteredItems = Collections2.filter(items, i -> Objects.nonNull(i.getJid()));
        getDatabase().blockingDao().set(account, filteredItems);
    }
}
