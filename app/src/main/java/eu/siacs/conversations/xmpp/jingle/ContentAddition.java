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

package eu.siacs.conversations.xmpp.jingle;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import eu.siacs.conversations.xmpp.jingle.stanzas.Content;
import java.util.Set;

public final class ContentAddition {

    public final Direction direction;
    public final Set<Summary> summary;

    private ContentAddition(Direction direction, Set<Summary> summary) {
        this.direction = direction;
        this.summary = summary;
    }

    public Set<Media> media() {
        return ImmutableSet.copyOf(Collections2.transform(summary, s -> s.media));
    }

    public static ContentAddition of(final Direction direction, final RtpContentMap rtpContentMap) {
        return new ContentAddition(direction, summary(rtpContentMap));
    }

    public static Set<Summary> summary(final RtpContentMap rtpContentMap) {
        return ImmutableSet.copyOf(
                Collections2.transform(
                        rtpContentMap.contents.entrySet(),
                        e -> {
                            final RtpContentMap.DescriptionTransport dt = e.getValue();
                            return new Summary(e.getKey(), dt.description.getMedia(), dt.senders);
                        }));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("direction", direction)
                .add("summary", summary)
                .toString();
    }

    public enum Direction {
        OUTGOING,
        INCOMING
    }

    public static final class Summary {
        public final String name;
        public final Media media;
        public final Content.Senders senders;

        private Summary(final String name, final Media media, final Content.Senders senders) {
            this.name = name;
            this.media = media;
            this.senders = senders;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Summary summary = (Summary) o;
            return Objects.equal(name, summary.name)
                    && media == summary.media
                    && senders == summary.senders;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, media, senders);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("media", media)
                    .add("senders", senders)
                    .toString();
        }
    }
}
