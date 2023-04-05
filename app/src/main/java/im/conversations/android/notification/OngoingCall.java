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

package im.conversations.android.notification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import eu.siacs.conversations.xmpp.jingle.AbstractJingleConnection;
import eu.siacs.conversations.xmpp.jingle.Media;
import java.util.Set;

public class OngoingCall {
    public final AbstractJingleConnection.Id id;
    public final Set<Media> media;
    public final boolean reconnecting;

    public OngoingCall(
            AbstractJingleConnection.Id id, Set<Media> media, final boolean reconnecting) {
        this.id = id;
        this.media = media;
        this.reconnecting = reconnecting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OngoingCall that = (OngoingCall) o;
        return reconnecting == that.reconnecting
                && Objects.equal(id, that.id)
                && Objects.equal(media, that.media);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, media, reconnecting);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("media", media)
                .add("reconnecting", reconnecting)
                .toString();
    }
}
