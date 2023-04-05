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
import com.google.common.collect.ImmutableList;
import im.conversations.android.xml.Element;
import im.conversations.android.xml.Namespace;
import java.util.Collection;
import java.util.List;

public class Group extends Element {

    private Group() {
        super("group", Namespace.JINGLE_APPS_GROUPING);
    }

    public Group(final String semantics, final Collection<String> identificationTags) {
        super("group", Namespace.JINGLE_APPS_GROUPING);
        this.setAttribute("semantics", semantics);
        for (String tag : identificationTags) {
            this.addChild(new Element("content").setAttribute("name", tag));
        }
    }

    public String getSemantics() {
        return this.getAttribute("semantics");
    }

    public List<String> getIdentificationTags() {
        final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
        for (final Element child : this.children) {
            if ("content".equals(child.getName())) {
                final String name = child.getAttribute("name");
                if (name != null) {
                    builder.add(name);
                }
            }
        }
        return builder.build();
    }

    public static Group ofSdpString(final String input) {
        ImmutableList.Builder<String> tagBuilder = new ImmutableList.Builder<>();
        final String[] parts = input.split(" ");
        if (parts.length >= 2) {
            final String semantics = parts[0];
            for (int i = 1; i < parts.length; ++i) {
                tagBuilder.add(parts[i]);
            }
            return new Group(semantics, tagBuilder.build());
        }
        return null;
    }

    public static Group upgrade(final Element element) {
        Preconditions.checkArgument("group".equals(element.getName()));
        Preconditions.checkArgument(Namespace.JINGLE_APPS_GROUPING.equals(element.getNamespace()));
        final Group group = new Group();
        group.setAttributes(element.getAttributes());
        group.setChildren(element.getChildren());
        return group;
    }
}
