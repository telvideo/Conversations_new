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

package im.conversations.android.axolotl;

import com.google.common.base.Objects;
import org.jxmpp.jid.BareJid;
import org.whispersystems.libsignal.SignalProtocolAddress;

public class AxolotlAddress extends SignalProtocolAddress {

    private final BareJid jid;

    public AxolotlAddress(final BareJid jid, int deviceId) {
        super(jid.toString(), deviceId);
        this.jid = jid;
    }

    public BareJid getJid() {
        return this.jid;
    }

    public static AxolotlAddress cast(final SignalProtocolAddress signalProtocolAddress) {
        if (signalProtocolAddress instanceof AxolotlAddress) {
            return (AxolotlAddress) signalProtocolAddress;
        }
        throw new IllegalArgumentException(
                String.format(
                        "This %s is not a %s",
                        SignalProtocolAddress.class.getSimpleName(),
                        AxolotlAddress.class.getSimpleName()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AxolotlAddress that = (AxolotlAddress) o;
        return Objects.equal(jid, that.jid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), jid);
    }
}
