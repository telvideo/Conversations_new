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

import com.google.common.collect.ArrayListMultimap;
import java.util.List;

public class SessionDescriptionBuilder {
    private int version;
    private String name;
    private String connectionData;
    private ArrayListMultimap<String, String> attributes;
    private List<SessionDescription.Media> media;

    public SessionDescriptionBuilder setVersion(int version) {
        this.version = version;
        return this;
    }

    public SessionDescriptionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SessionDescriptionBuilder setConnectionData(String connectionData) {
        this.connectionData = connectionData;
        return this;
    }

    public SessionDescriptionBuilder setAttributes(ArrayListMultimap<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public SessionDescriptionBuilder setMedia(List<SessionDescription.Media> media) {
        this.media = media;
        return this;
    }

    public SessionDescription createSessionDescription() {
        return new SessionDescription(version, name, connectionData, attributes, media);
    }
}
