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

package eu.siacs.conversations.xmpp.jingle.stanzas;

import com.google.common.base.Preconditions;
import im.conversations.android.xml.Element;
import im.conversations.android.xml.Namespace;

public class IbbTransportInfo extends GenericTransportInfo {

    private IbbTransportInfo(final String name, final String xmlns) {
        super(name, xmlns);
    }

    public IbbTransportInfo(final String transportId, final int blockSize) {
        super("transport", Namespace.JINGLE_TRANSPORTS_IBB);
        Preconditions.checkNotNull(transportId, "Transport ID can not be null");
        Preconditions.checkArgument(blockSize > 0, "Block size must be larger than 0");
        this.setAttribute("block-size", blockSize);
        this.setAttribute("sid", transportId);
    }

    public String getTransportId() {
        return this.getAttribute("sid");
    }

    public int getBlockSize() {
        final String blockSize = this.getAttribute("block-size");
        if (blockSize == null) {
            return 0;
        }
        try {
            return Integer.parseInt(blockSize);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static IbbTransportInfo upgrade(final Element element) {
        Preconditions.checkArgument(
                "transport".equals(element.getName()), "Name of provided element is not transport");
        Preconditions.checkArgument(
                Namespace.JINGLE_TRANSPORTS_IBB.equals(element.getNamespace()),
                "Element does not match ibb transport namespace");
        final IbbTransportInfo transportInfo =
                new IbbTransportInfo("transport", Namespace.JINGLE_TRANSPORTS_IBB);
        transportInfo.setAttributes(element.getAttributes());
        transportInfo.setChildren(element.getChildren());
        return transportInfo;
    }
}
