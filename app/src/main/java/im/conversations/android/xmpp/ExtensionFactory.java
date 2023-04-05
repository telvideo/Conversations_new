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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import im.conversations.android.xml.Element;
import im.conversations.android.xmpp.model.Extension;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ExtensionFactory {

    public static Element create(final String name, final String namespace) {
        final Class<? extends Extension> clazz = of(name, namespace);
        if (clazz == null) {
            return new Element(name, namespace);
        }
        final Constructor<? extends Element> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(
                    String.format("%s has no default constructor", clazz.getName()));
        }
        try {
            return constructor.newInstance();
        } catch (final IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
            throw new IllegalStateException(
                    String.format("%s has inaccessible default constructor", clazz.getName()));
        }
    }

    private static Class<? extends Extension> of(final String name, final String namespace) {
        return Extensions.EXTENSION_CLASS_MAP.get(new Id(name, namespace));
    }

    public static Id id(final Class<? extends Extension> clazz) {
        return Extensions.EXTENSION_CLASS_MAP.inverse().get(clazz);
    }

    private ExtensionFactory() {}

    public static class Id {
        public final String name;
        public final String namespace;

        public Id(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equal(name, id.name) && Objects.equal(namespace, id.namespace);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, namespace);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("namespace", namespace)
                    .toString();
        }
    }
}
