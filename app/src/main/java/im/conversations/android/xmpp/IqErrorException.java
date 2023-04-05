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

package im.conversations.android.xmpp;

import com.google.common.base.Strings;
import im.conversations.android.xmpp.model.error.Condition;
import im.conversations.android.xmpp.model.error.Error;
import im.conversations.android.xmpp.model.stanza.Iq;

public class IqErrorException extends Exception {

    private final Iq response;

    public IqErrorException(Iq response) {
        super(getErrorText(response));
        this.response = response;
    }

    public Error getError() {
        return this.response.getError();
    }

    private static String getErrorText(final Iq response) {
        final var error = response.getError();
        final var text = error == null ? null : error.getText();
        final var textContent = text == null ? null : text.getContent();
        if (Strings.isNullOrEmpty(textContent)) {
            final var condition = error == null ? null : error.getExtension(Condition.class);
            return condition == null ? null : condition.getName();
        }
        return textContent;
    }

    public Iq getResponse() {
        return this.response;
    }
}
