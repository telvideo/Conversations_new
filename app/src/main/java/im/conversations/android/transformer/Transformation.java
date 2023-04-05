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

package im.conversations.android.transformer;

import im.conversations.android.xmpp.model.stanza.Message;
import java.time.Instant;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;

abstract class Transformation {

    public final Instant receivedAt;
    public final Jid to;
    public final Jid from;
    public final Jid remote;
    public final Message.Type type;
    public final String messageId;
    public final String stanzaId;

    public final String occupantId;

    public Transformation(
            final Instant receivedAt,
            final Jid to,
            final Jid from,
            final Jid remote,
            final Message.Type type,
            final String messageId,
            final String stanzaId,
            final String occupantId) {
        this.receivedAt = receivedAt;
        this.to = to;
        this.from = from;
        this.remote = remote;
        this.type = type;
        this.messageId = messageId;
        this.stanzaId = stanzaId;
        this.occupantId = occupantId;
    }

    public BareJid fromBare() {
        return from == null ? null : from.asBareJid();
    }

    public Resourcepart fromResource() {
        return from == null ? null : from.getResourceOrNull();
    }

    public BareJid toBare() {
        return to == null ? null : to.asBareJid();
    }

    public Resourcepart toResource() {
        return to == null ? null : to.getResourceOrNull();
    }

    public Instant sentAt() {
        // TODO get Delay that matches sender; return receivedAt if not found
        return receivedAt;
    }

    public boolean outgoing() {
        // TODO handle case for self addressed (to == from)
        return remote.asBareJid().equals(toBare());
    }
}
