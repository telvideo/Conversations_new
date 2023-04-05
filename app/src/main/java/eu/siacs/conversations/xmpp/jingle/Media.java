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

package eu.siacs.conversations.xmpp.jingle;

import com.google.common.collect.ImmutableSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;

public enum Media {
    VIDEO,
    AUDIO,
    UNKNOWN;

    @Override
    @Nonnull
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }

    public static Media of(String value) {
        try {
            return value == null ? UNKNOWN : Media.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public static boolean audioOnly(Set<Media> media) {
        return ImmutableSet.of(AUDIO).equals(media);
    }

    public static boolean videoOnly(Set<Media> media) {
        return ImmutableSet.of(VIDEO).equals(media);
    }
}
