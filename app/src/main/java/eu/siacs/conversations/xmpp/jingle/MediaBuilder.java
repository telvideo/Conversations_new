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

public class MediaBuilder {
    private String media;
    private int port;
    private String protocol;
    private List<Integer> formats;
    private String connectionData;
    private ArrayListMultimap<String, String> attributes;

    public MediaBuilder setMedia(String media) {
        this.media = media;
        return this;
    }

    public MediaBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public MediaBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public MediaBuilder setFormats(List<Integer> formats) {
        this.formats = formats;
        return this;
    }

    public MediaBuilder setConnectionData(String connectionData) {
        this.connectionData = connectionData;
        return this;
    }

    public MediaBuilder setAttributes(ArrayListMultimap<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public SessionDescription.Media createMedia() {
        return new SessionDescription.Media(
                media, port, protocol, formats, connectionData, attributes);
    }
}
