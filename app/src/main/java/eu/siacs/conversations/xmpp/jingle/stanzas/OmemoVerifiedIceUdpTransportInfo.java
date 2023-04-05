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

import im.conversations.android.xml.Namespace;

public class OmemoVerifiedIceUdpTransportInfo extends IceUdpTransportInfo {

    public void ensureNoPlaintextFingerprint() {
        if (this.findChild("fingerprint", Namespace.JINGLE_APPS_DTLS) != null) {
            throw new IllegalStateException(
                    "OmemoVerifiedIceUdpTransportInfo contains plaintext fingerprint");
        }
    }

    public static IceUdpTransportInfo upgrade(final IceUdpTransportInfo transportInfo) {
        if (transportInfo.hasChild("fingerprint", Namespace.JINGLE_APPS_DTLS)) {
            return transportInfo;
        }
        if (transportInfo.hasChild("fingerprint", Namespace.OMEMO_DTLS_SRTP_VERIFICATION)) {
            final OmemoVerifiedIceUdpTransportInfo omemoVerifiedIceUdpTransportInfo =
                    new OmemoVerifiedIceUdpTransportInfo();
            omemoVerifiedIceUdpTransportInfo.setAttributes(transportInfo.getAttributes());
            omemoVerifiedIceUdpTransportInfo.setChildren(transportInfo.getChildren());
            return omemoVerifiedIceUdpTransportInfo;
        }
        return transportInfo;
    }
}
