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

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.state.SignalProtocolStore;

public class AxolotlSession {

    public final AxolotlAddress axolotlAddress;
    public final IdentityKey identityKey;
    public final SessionCipher sessionCipher;

    private AxolotlSession(
            AxolotlAddress axolotlAddress,
            final IdentityKey identityKey,
            SessionCipher sessionCipher) {
        this.axolotlAddress = axolotlAddress;
        this.identityKey = identityKey;
        this.sessionCipher = sessionCipher;
    }

    public static AxolotlSession of(
            final SignalProtocolStore signalProtocolStore,
            final IdentityKey identityKey,
            final AxolotlAddress axolotlAddress) {
        final var sessionCipher = new SessionCipher(signalProtocolStore, axolotlAddress);
        return new AxolotlSession(axolotlAddress, identityKey, sessionCipher);
    }
}
