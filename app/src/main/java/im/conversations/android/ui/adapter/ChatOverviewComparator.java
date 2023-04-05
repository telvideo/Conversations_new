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

package im.conversations.android.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import im.conversations.android.database.model.ChatOverviewItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatOverviewComparator extends DiffUtil.ItemCallback<ChatOverviewItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatOverviewComparator.class);

    @Override
    public boolean areItemsTheSame(
            @NonNull ChatOverviewItem oldItem, @NonNull ChatOverviewItem newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(
            @NonNull ChatOverviewItem oldItem, @NonNull ChatOverviewItem newItem) {
        final boolean areContentsTheSame = oldItem.equals(newItem);
        if (!areContentsTheSame) {
            LOGGER.info("chat {} got modified", oldItem.id);
        }
        return areContentsTheSame;
    }
}
