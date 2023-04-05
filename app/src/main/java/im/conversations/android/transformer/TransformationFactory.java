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

import android.content.Context;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import im.conversations.android.database.model.StanzaId;
import im.conversations.android.xml.Namespace;
import im.conversations.android.xmpp.Entity;
import im.conversations.android.xmpp.XmppConnection;
import im.conversations.android.xmpp.manager.DiscoManager;
import im.conversations.android.xmpp.model.Extension;
import im.conversations.android.xmpp.model.muc.user.MucUser;
import im.conversations.android.xmpp.model.occupant.OccupantId;
import im.conversations.android.xmpp.model.stanza.Message;
import java.time.Instant;
import java.util.List;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;

public class TransformationFactory extends XmppConnection.Delegate {

    private final Mode mode;

    public TransformationFactory(Context context, XmppConnection connection, final Mode mode) {
        super(context, connection);
        this.mode = mode;
    }

    public MessageTransformation create(final Message message, final StanzaId stanzaId) {
        Preconditions.checkState(
                mode == Mode.LIVE,
                "Creating a MessageTransformation with automatic timestamp is only allowed in live"
                        + " mode");
        return create(message, stanzaId == null ? null : stanzaId.id, Instant.now(), null);
    }

    public MessageTransformation create(
            final Message message,
            final String stanzaId,
            final Instant receivedAt,
            final List<Extension> privilegedExtensions) {
        if (privilegedExtensions != null) {
            Preconditions.checkArgument(
                    this.mode == Mode.ARCHIVE,
                    "Privileged extensions can only supplied in archive mode");
        }
        final var boundAddress = connection.getBoundAddress().asBareJid();
        final var from = message.getFrom();
        final var to = message.getTo();
        final Jid remote;
        if (from == null || from.asBareJid().equals(boundAddress)) {
            remote = to == null ? boundAddress : to;
        } else {
            remote = from;
        }
        final String occupantId;
        if (message.getType() == Message.Type.GROUPCHAT && message.hasExtension(OccupantId.class)) {
            if (from != null
                    && getManager(DiscoManager.class)
                            .hasFeature(
                                    Entity.discoItem(from.asBareJid()), Namespace.OCCUPANT_ID)) {
                occupantId = message.getExtension(OccupantId.class).getId();
            } else {
                occupantId = null;
            }
        } else {
            occupantId = null;
        }
        final BareJid senderIdentity;
        if (message.getType() == Message.Type.GROUPCHAT) {
            final var mucUser =
                    mode == Mode.ARCHIVE ? getExtension(privilegedExtensions, MucUser.class) : null;
            final var mucUserItem = mucUser == null ? null : mucUser.getItem();
            final Jid mucUserJid = mucUserItem == null ? null : mucUserItem.getJid();
            if (mucUserJid != null) {
                senderIdentity = mucUserJid.asBareJid();
            } else if (occupantId != null) {
                senderIdentity =
                        getDatabase()
                                .presenceDao()
                                .getMucUserJidByOccupantId(
                                        getAccount(), from.asBareJid(), occupantId);
            } else if (mode == Mode.LIVE && from != null && from.hasResource()) {
                senderIdentity =
                        getDatabase()
                                .presenceDao()
                                .getMucUserJidByResource(
                                        getAccount(), from.asBareJid(), from.getResourceOrThrow());
            } else {
                senderIdentity = null;
            }
        } else {
            senderIdentity = from == null ? boundAddress : from.asBareJid();
        }
        return MessageTransformation.of(
                message, receivedAt, remote, stanzaId, senderIdentity, occupantId);
    }

    private static <E extends Extension> E getExtension(
            final List<Extension> extensions, final Class<E> clazz) {
        final var extension =
                extensions == null ? null : Iterables.find(extensions, clazz::isInstance, null);
        return extension == null ? null : clazz.cast(extension);
    }

    public enum Mode {
        LIVE,
        ARCHIVE
    }
}
