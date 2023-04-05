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

import java.nio.charset.StandardCharsets;
import org.whispersystems.libsignal.IdentityKey;

public class AxolotlPayload {

    public final AxolotlAddress axolotlAddress;
    public final IdentityKey identityKey;
    public final boolean preKeyMessage;
    public final boolean inDeviceList;
    public final byte[] payload;

    public AxolotlPayload(
            AxolotlAddress axolotlAddress,
            final IdentityKey identityKey,
            final boolean preKeyMessage,
            final boolean inDeviceList,
            byte[] payload) {
        this.axolotlAddress = axolotlAddress;
        this.identityKey = identityKey;
        this.preKeyMessage = preKeyMessage;
        this.inDeviceList = inDeviceList;
        this.payload = payload;
    }

    public String payloadAsString() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public boolean hasPayload() {
        return payload != null;
    }
}
