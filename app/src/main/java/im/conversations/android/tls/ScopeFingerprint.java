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

package im.conversations.android.tls;

import androidx.annotation.NonNull;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import im.conversations.android.xmpp.manager.TrustManager;
import java.nio.ByteBuffer;

public class ScopeFingerprint {
    public final String scope;
    public final ByteBuffer fingerprint;

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scope", scope)
                .add("fingerprint", TrustManager.fingerprint(fingerprint.array()))
                .toString();
    }

    public ScopeFingerprint(final String scope, final byte[] fingerprint) {
        this(scope, ByteBuffer.wrap(fingerprint));
    }

    public ScopeFingerprint(final String scope, final ByteBuffer fingerprint) {
        this.scope = scope;
        this.fingerprint = fingerprint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopeFingerprint that = (ScopeFingerprint) o;
        return Objects.equal(scope, that.scope) && Objects.equal(fingerprint, that.fingerprint);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scope, fingerprint);
    }
}
