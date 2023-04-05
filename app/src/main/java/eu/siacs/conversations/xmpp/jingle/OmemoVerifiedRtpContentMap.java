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

import eu.siacs.conversations.xmpp.jingle.stanzas.Group;
import eu.siacs.conversations.xmpp.jingle.stanzas.OmemoVerifiedIceUdpTransportInfo;
import java.util.Map;

public class OmemoVerifiedRtpContentMap extends RtpContentMap {
    public OmemoVerifiedRtpContentMap(Group group, Map<String, DescriptionTransport> contents) {
        super(group, contents);
        for (final DescriptionTransport descriptionTransport : contents.values()) {
            if (descriptionTransport.transport instanceof OmemoVerifiedIceUdpTransportInfo) {
                ((OmemoVerifiedIceUdpTransportInfo) descriptionTransport.transport)
                        .ensureNoPlaintextFingerprint();
                continue;
            }
            throw new IllegalStateException(
                    "OmemoVerifiedRtpContentMap contains non-verified transport info");
        }
    }
}
