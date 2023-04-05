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
import im.conversations.android.database.entity.ServiceRecordCacheEntity;
import im.conversations.android.database.model.Account;
import im.conversations.android.dns.ServiceRecord;

@Dao
public abstract class ServiceRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(ServiceRecordCacheEntity entity);

    public void insert(final Account account, final ServiceRecord serviceRecord) {
        insert(
                ServiceRecordCacheEntity.of(
                        account, account.address.getDomain().toString(), serviceRecord));
    }

    @Query(
            "SELECT ip,hostname,port,directTls,priority,authenticated FROM service_record_cache"
                    + " WHERE accountId=:account AND domain=:domain LIMIT 1")
    protected abstract ServiceRecord getCachedServiceRecord(
            final long account, final String domain);

    public ServiceRecord getCachedServiceRecord(final Account account) {
        return getCachedServiceRecord(account.id, account.address.getDomain().toString());
    }
}
