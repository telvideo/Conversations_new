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

package eu.siacs.conversations.xmpp.jingle.stanzas;

import com.google.common.base.Preconditions;
import im.conversations.android.xml.Element;

public class GenericDescription extends Element {

    GenericDescription(String name, final String namespace) {
        super(name, namespace);
    }

    public static GenericDescription upgrade(final Element element) {
        Preconditions.checkArgument("description".equals(element.getName()));
        final GenericDescription description =
                new GenericDescription("description", element.getNamespace());
        description.setAttributes(element.getAttributes());
        description.setChildren(element.getChildren());
        return description;
    }
}
