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

package im.conversations.android.util;

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import im.conversations.android.database.model.MessageContent;
import im.conversations.android.database.model.PartType;
import java.util.List;

public final class TextContents {

    private TextContents() {}

    public static String toText(
            final List<MessageContent> messageContents,
            final boolean removeFallback,
            final int inReplyToFallbackStart,
            final int inReplyToFallbackEnd) {
        final var textContents = Collections2.filter(messageContents, c -> c.type == PartType.TEXT);
        if (textContents.size() == 1 && removeFallback) {
            final String body = Strings.nullToEmpty(Iterables.getOnlyElement(textContents).body);
            if (inReplyToFallbackEnd > inReplyToFallbackStart
                    && inReplyToFallbackEnd <= body.length()) {
                return body.substring(0, inReplyToFallbackStart)
                        + body.substring(inReplyToFallbackEnd);
            }
        }
        final var anyTextContent = Iterables.getFirst(textContents, null);
        return anyTextContent == null ? null : anyTextContent.body;
    }
}
