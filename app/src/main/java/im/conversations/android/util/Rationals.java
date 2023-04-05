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

import android.util.Rational;

public final class Rationals {

    // between 2.39:1 and 1:2.39 (inclusive).
    private static final Rational MIN = new Rational(100, 239);
    private static final Rational MAX = new Rational(239, 100);

    private Rationals() {}

    public static Rational clip(final Rational input) {
        if (input.compareTo(MIN) < 0) {
            return MIN;
        }
        if (input.compareTo(MAX) > 0) {
            return MAX;
        }
        return input;
    }
}
