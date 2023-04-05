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

package im.conversations.android.database;

import im.conversations.android.database.model.ChatType;
import java.util.Arrays;
import org.jxmpp.jid.BareJid;

public interface KnownSender {

    default boolean isKnownSender() {
        final var chatType = getChatType();
        final var membersOnlyNonAnonymous = isMembersOnlyNonAnonymous();
        final var sender = getSender();
        return chatType == ChatType.INDIVIDUAL
                || (Arrays.asList(ChatType.MUC, ChatType.MUC_PM).contains(chatType)
                        && membersOnlyNonAnonymous
                        && sender != null);
    }

    ChatType getChatType();

    boolean isMembersOnlyNonAnonymous();

    BareJid getSender();
}
