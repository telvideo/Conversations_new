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

import im.conversations.android.IDs;
import im.conversations.android.database.model.Account;
import org.junit.Assert;
import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class AccountTest {

    @Test
    public void testEquals() throws XmppStringprepException {
        final var seed = IDs.seed();
        final var accountOne = new Account(1L, JidCreate.bareFrom("test@example.com"), seed);
        final var seedCopy = new byte[seed.length];
        System.arraycopy(seed, 0, seedCopy, 0, seedCopy.length);
        final var accountTwo = new Account(1L, JidCreate.bareFrom("test@example.com"), seedCopy);
        Assert.assertEquals(accountOne, accountTwo);
        Assert.assertEquals(accountOne.hashCode(), accountTwo.hashCode());
    }
}
