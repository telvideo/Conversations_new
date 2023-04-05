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
import im.conversations.android.database.model.MessageAdapterItem;
import im.conversations.android.database.model.MessageWithContentReactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageComparator extends DiffUtil.ItemCallback<MessageAdapterItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageComparator.class);

    @Override
    public boolean areItemsTheSame(
            @NonNull MessageAdapterItem oldItem, @NonNull MessageAdapterItem newItem) {
        if (oldItem instanceof MessageWithContentReactions oldMessage
                && newItem instanceof MessageWithContentReactions newMessage) {
            return oldMessage.id == newMessage.id;
        } else if (oldItem instanceof MessageAdapterItem.MessageDateSeparator oldSeparator
                && newItem instanceof MessageAdapterItem.MessageDateSeparator newSeparator) {
            return oldSeparator.date.equals(newSeparator.date);
        } else {
            return false;
        }
    }

    @Override
    public boolean areContentsTheSame(
            @NonNull MessageAdapterItem oldItem, @NonNull MessageAdapterItem newItem) {
        if (oldItem instanceof MessageWithContentReactions oldMessage
                && newItem instanceof MessageWithContentReactions newMessage) {
            final var areContentsTheSame = oldMessage.equals(newMessage);
            if (!areContentsTheSame) {
                LOGGER.info("Message {} got modified", oldMessage.id);
            }
            return areContentsTheSame;
        } else if (oldItem instanceof MessageAdapterItem.MessageDateSeparator oldSeparator
                && newItem instanceof MessageAdapterItem.MessageDateSeparator newSeparator) {
            return oldSeparator.date.equals(newSeparator.date);
        } else {
            return false;
        }
    }
}
