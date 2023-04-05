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

import androidx.room.Query;
import org.jxmpp.jid.Jid;

public abstract class BaseDao {

    @Query(
            "SELECT EXISTS (SELECT disco_item.id FROM disco_item JOIN disco_feature on"
                    + " disco_item.discoId=disco_feature.discoId WHERE accountId=:account AND"
                    + " address=:entity AND feature=:feature)")
    protected abstract boolean hasDiscoItemFeature(
            final long account, final Jid entity, final String feature);
}
