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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import im.conversations.android.xml.Element;
import im.conversations.android.xml.XmlElementReader;
import im.conversations.android.xmpp.model.delay.Delay;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class TimestampTest {

    @Test
    public void testZuluNoMillis() throws IOException {
        final String xml =
                """
                        <delay xmlns='urn:xmpp:delay'
                             from='capulet.com'
                             stamp='2002-09-10T23:08:25Z'/>""";
        final Element element = XmlElementReader.read(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(element, instanceOf(Delay.class));
        final Delay delay = (Delay) element;
        assertEquals(1031699305000L, delay.getStamp().toEpochMilli());
    }

    @Test
    public void testZuluWithMillis() throws IOException {
        final String xml =
                """
                        <delay xmlns='urn:xmpp:delay'
                             from='capulet.com'
                             stamp='2002-09-10T23:08:25.023Z'/>""";
        final Element element = XmlElementReader.read(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(element, instanceOf(Delay.class));
        final Delay delay = (Delay) element;
        assertEquals(1031699305023L, delay.getStamp().toEpochMilli());
    }
}
