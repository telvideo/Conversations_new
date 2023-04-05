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
import im.conversations.android.database.model.StanzaId;
import im.conversations.android.xml.Namespace;
import im.conversations.android.xmpp.Entity;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.stanza.Message;
import org.jxmpp.jid.Jid;

public class StanzaIdManager extends AbstractManager {

    public StanzaIdManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public StanzaId getStanzaId(final Message message) {
        final Jid by;
        if (message.getType() == Message.Type.GROUPCHAT) {
            final var from = message.getFrom();
            if (from == null) {
                return null;
            }
            by = from.asBareJid();
        } else {
            by = connection.getBoundAddress().asBareJid();
        }
        if (message.hasExtension(im.conversations.android.xmpp.model.unique.StanzaId.class)
                && getManager(DiscoManager.class)
                        .hasFeature(Entity.discoItem(by), Namespace.STANZA_IDS)) {
            return getStanzaIdBy(message, by);
        } else {
            return null;
        }
    }

    private static StanzaId getStanzaIdBy(final Message message, final Jid by) {
        for (final var stanzaId :
                message.getExtensions(im.conversations.android.xmpp.model.unique.StanzaId.class)) {
            final var id = stanzaId.getId();
            if (by.equals(stanzaId.getBy()) && id != null) {
                return new StanzaId(id, by);
            }
        }
        return null;
    }
}
