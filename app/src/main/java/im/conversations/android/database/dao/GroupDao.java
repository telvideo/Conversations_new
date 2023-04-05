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

import androidx.room.Insert;
import androidx.room.Query;
import im.conversations.android.database.entity.GroupEntity;

public abstract class GroupDao {

    public long getOrCreateId(final String name) {
        final Long existing = getGroupId(name);
        if (existing != null) {
            return existing;
        }
        return insert(GroupEntity.of(name));
    }

    @Query("SELECT id FROM `group` WHERE name=:name")
    abstract Long getGroupId(final String name);

    @Insert
    abstract Long insert(GroupEntity groupEntity);

    @Query(
            "DELETE from `group` WHERE id NOT IN(SELECT groupId FROM roster_group) AND id NOT"
                    + " IN(SELECT groupId FROM bookmark_group)")
    abstract void deleteEmptyGroups();
}
