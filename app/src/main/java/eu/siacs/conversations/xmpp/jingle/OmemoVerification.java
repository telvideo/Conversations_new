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
import com.google.common.base.Preconditions;
import im.conversations.android.axolotl.AxolotlService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.whispersystems.libsignal.IdentityKey;

public class OmemoVerification {

    private final AtomicBoolean deviceIdWritten = new AtomicBoolean(false);
    private final AtomicBoolean identityKeyWritten = new AtomicBoolean(false);
    private Integer deviceId;
    private IdentityKey identityKey;

    public void setDeviceId(final Integer id) {
        if (deviceIdWritten.compareAndSet(false, true)) {
            this.deviceId = id;
            return;
        }
        throw new IllegalStateException("Device Id has already been set");
    }

    public int getDeviceId() {
        Preconditions.checkNotNull(this.deviceId, "Device ID is null");
        return this.deviceId;
    }

    public boolean hasDeviceId() {
        return this.deviceId != null;
    }

    public void setSessionFingerprint(final IdentityKey identityKey) {
        Preconditions.checkNotNull(identityKey, "IdentityKey must not be null");
        if (identityKeyWritten.compareAndSet(false, true)) {
            this.identityKey = identityKey;
            return;
        }
        throw new IllegalStateException("Identity Key has already been set");
    }

    public IdentityKey getFingerprint() {
        return this.identityKey;
    }

    public void setOrEnsureEqual(AxolotlService.OmemoVerifiedPayload<?> omemoVerifiedPayload) {
        setOrEnsureEqual(omemoVerifiedPayload.getDeviceId(), omemoVerifiedPayload.getFingerprint());
    }

    public void setOrEnsureEqual(final int deviceId, final IdentityKey identityKey) {
        Preconditions.checkNotNull(identityKey, "IdentityKey must not be null");
        if (this.deviceIdWritten.get() || this.identityKeyWritten.get()) {
            if (this.identityKey == null) {
                throw new IllegalStateException(
                        "No session fingerprint has been previously provided");
            }
            if (!identityKey.equals(this.identityKey)) {
                throw new SecurityException("IdentityKeys did not match");
            }
            if (this.deviceId == null) {
                throw new IllegalStateException("No Device Id has been previously provided");
            }
            if (this.deviceId != deviceId) {
                throw new IllegalStateException("Device Ids did not match");
            }
        } else {
            this.setSessionFingerprint(identityKey);
            this.setDeviceId(deviceId);
        }
    }

    public boolean hasFingerprint() {
        return this.identityKey != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceId", deviceId)
                .add("fingerprint", identityKey)
                .toString();
    }
}
