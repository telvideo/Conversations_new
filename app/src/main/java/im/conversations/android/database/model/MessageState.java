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

package im.conversations.android.database.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import im.conversations.android.transformer.MessageTransformation;
import im.conversations.android.xmpp.model.error.Condition;
import im.conversations.android.xmpp.model.error.Error;
import im.conversations.android.xmpp.model.error.Text;
import im.conversations.android.xmpp.model.stanza.Message;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.parts.Resourcepart;

public class MessageState {

    public final BareJid fromBare;

    public final Resourcepart fromResource;

    public final StateType type;

    public final String errorCondition;

    public final String errorText;

    public MessageState(
            BareJid fromBare,
            Resourcepart fromResource,
            StateType type,
            String errorCondition,
            String errorText) {
        this.fromBare = fromBare;
        this.fromResource = fromResource;
        this.type = type;
        this.errorCondition = errorCondition;
        this.errorText = errorText;
    }

    public static MessageState error(final MessageTransformation transformation) {
        Preconditions.checkArgument(transformation.type == Message.Type.ERROR);
        final Error error = transformation.getExtension(Error.class);
        final Condition condition = error == null ? null : error.getCondition();
        final Text text = error == null ? null : error.getText();
        return new MessageState(
                transformation.fromBare(),
                transformation.fromResource(),
                StateType.ERROR,
                condition == null ? null : condition.getName(),
                text == null ? null : text.getContent());
    }

    public static MessageState delivered(final MessageTransformation transformation) {
        return new MessageState(
                transformation.fromBare(),
                transformation.fromResource(),
                StateType.DELIVERED,
                null,
                null);
    }

    public static MessageState displayed(final MessageTransformation transformation) {
        return new MessageState(
                transformation.fromBare(),
                transformation.fromResource(),
                StateType.DISPLAYED,
                null,
                null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageState that = (MessageState) o;
        return Objects.equal(fromBare, that.fromBare)
                && Objects.equal(fromResource, that.fromResource)
                && type == that.type
                && Objects.equal(errorCondition, that.errorCondition)
                && Objects.equal(errorText, that.errorText);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fromBare, fromResource, type, errorCondition, errorText);
    }
}
