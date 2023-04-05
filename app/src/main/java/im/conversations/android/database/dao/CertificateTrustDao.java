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

package im.conversations.android.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import im.conversations.android.database.entity.CertificateTrustEntity;
import im.conversations.android.database.model.Account;
import im.conversations.android.tls.ScopeFingerprint;

@Dao
public abstract class CertificateTrustDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(final CertificateTrustEntity certificateTrustEntity);

    @Query(
            "SELECT EXISTS (SELECT id FROM certificate_trust WHERE accountId=:account AND"
                    + " scope=:scope AND fingerprint=:fingerprint)")
    protected abstract boolean isTrusted(
            final long account, final String scope, final byte[] fingerprint);

    public boolean isTrusted(final Account account, final ScopeFingerprint scopeFingerprint) {
        return isTrusted(account.id, scopeFingerprint.scope, scopeFingerprint.fingerprint.array());
    }
}
