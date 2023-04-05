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
import com.google.common.collect.Collections2;
import im.conversations.android.database.entity.AvatarAdditionalEntity;
import im.conversations.android.database.entity.AvatarEntity;
import im.conversations.android.database.model.Account;
import im.conversations.android.xmpp.model.avatar.Info;
import java.util.Collection;
import org.jxmpp.jid.BareJid;

@Dao
public abstract class AvatarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract long insert(AvatarEntity avatar);

    @Insert
    protected abstract void insert(Collection<AvatarAdditionalEntity> entities);

    public void set(
            final Account account,
            final BareJid address,
            final Info thumbnail,
            final Collection<Info> additional) {
        final long id = insert(AvatarEntity.of(account, address, thumbnail));
        insert(Collections2.transform(additional, a -> AvatarAdditionalEntity.of(id, a)));
    }
}
