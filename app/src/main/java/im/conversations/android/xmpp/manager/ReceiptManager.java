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
import com.google.common.base.Strings;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.model.DeliveryReceiptRequest;
import im.conversations.android.xmpp.model.markers.Markable;
import im.conversations.android.xmpp.model.receipts.Received;
import im.conversations.android.xmpp.model.receipts.Request;
import im.conversations.android.xmpp.model.stanza.Message;
import java.util.Collection;
import org.jxmpp.jid.Jid;

public class ReceiptManager extends AbstractManager {

    public ReceiptManager(Context context, XmppConnection connection) {
        super(context, connection);
    }

    public void received(
            final Jid from,
            final String id,
            Collection<DeliveryReceiptRequest> deliveryReceiptRequests) {
        if (deliveryReceiptRequests.isEmpty() || Strings.isNullOrEmpty(id)) {
            return;
        }
        // TODO check roster
        final Message response = new Message();
        response.setTo(from);
        for (final DeliveryReceiptRequest request : deliveryReceiptRequests) {
            if (request instanceof Request) {
                final var received = response.addExtension(new Received());
                received.setId(id);
            } else if (request instanceof Markable) {
                final var received =
                        response.addExtension(
                                new im.conversations.android.xmpp.model.markers.Received());
                received.setId(id);
            }
        }
        connection.sendMessagePacket(response);
    }
}
