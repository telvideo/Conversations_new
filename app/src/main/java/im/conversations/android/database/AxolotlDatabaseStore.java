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

import android.content.Context;
import im.conversations.android.AbstractAccountService;
import im.conversations.android.AppSettings;
import im.conversations.android.axolotl.AxolotlAddress;
import im.conversations.android.database.dao.AxolotlDao;
import im.conversations.android.database.model.Account;
import im.conversations.android.database.model.Trust;
import java.util.List;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

public class AxolotlDatabaseStore extends AbstractAccountService implements SignalProtocolStore {

    private AppSettings appSettings;

    public AxolotlDatabaseStore(
            final Account account,
            final Context context,
            final ConversationsDatabase conversationsDatabase) {
        super(account, conversationsDatabase);
        this.appSettings = new AppSettings(context);
    }

    private AxolotlDao axolotlDao() {
        return database.axolotlDao();
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        return axolotlDao().getOrCreateIdentityKeyPair(account);
    }

    @Override
    public int getLocalRegistrationId() {
        return account.getPublicDeviceIdInt();
    }

    @Override
    public boolean saveIdentity(
            final SignalProtocolAddress signalProtocolAddress, final IdentityKey identityKey) {
        final var address = AxolotlAddress.cast(signalProtocolAddress);
        final boolean isBTBVEnabled = appSettings.isBTBVEnabled();
        return database.runInTransaction(
                () -> {
                    final Trust trust;
                    if (!isBTBVEnabled
                            || axolotlDao().isAnyIdentityVerified(account, address.getJid())) {
                        trust = Trust.UNDECIDED;
                    } else {
                        trust = Trust.TRUSTED;
                    }
                    return axolotlDao().setIdentity(account, address.getJid(), identityKey, trust);
                });
    }

    @Override
    public boolean isTrustedIdentity(
            SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        // TODO return false for Direction==Sending and Trust == untrusted
        return true;
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        final var preKey = axolotlDao().getPreKey(account.id, preKeyId);
        if (preKey == null) {
            throw new InvalidKeyIdException(String.format("PreKey %d does not exist", preKeyId));
        }
        return preKey;
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord preKeyRecord) {
        axolotlDao().setPreKey(account, preKeyId, preKeyRecord);
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        return axolotlDao().hasPreKey(account.id, preKeyId);
    }

    @Override
    public void removePreKey(int preKeyId) {
        axolotlDao().markPreKeyAsRemoved(account.id, preKeyId);
    }

    @Override
    public SessionRecord loadSession(final SignalProtocolAddress signalProtocolAddress) {
        final var address = AxolotlAddress.cast(signalProtocolAddress);
        final var sessionRecord =
                axolotlDao().getSessionRecord(account.id, address.getJid(), address.getDeviceId());
        return sessionRecord == null ? new SessionRecord() : sessionRecord;
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        return axolotlDao().getSessionDeviceIds(account.id, name);
    }

    @Override
    public void storeSession(SignalProtocolAddress signalProtocolAddress, SessionRecord record) {
        final var address = AxolotlAddress.cast(signalProtocolAddress);
        axolotlDao().setSessionRecord(account, address.getJid(), address.getDeviceId(), record);
    }

    @Override
    public boolean containsSession(SignalProtocolAddress signalProtocolAddress) {
        final var address = AxolotlAddress.cast(signalProtocolAddress);
        return axolotlDao().hasSession(account.id, address.getJid(), address.getDeviceId());
    }

    @Override
    public void deleteSession(SignalProtocolAddress signalProtocolAddress) {
        final var address = AxolotlAddress.cast(signalProtocolAddress);
        axolotlDao().deleteSession(account.id, address.getJid(), address.getDeviceId());
    }

    @Override
    public void deleteAllSessions(String name) {
        axolotlDao().deleteSessions(account.id, name);
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        final var signedPreKeyRecord = axolotlDao().getSignedPreKey(account.id, signedPreKeyId);
        if (signedPreKeyRecord == null) {
            throw new InvalidKeyIdException(
                    String.format("signedPreKey %d not found", signedPreKeyId));
        }
        return signedPreKeyRecord;
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return axolotlDao().getSignedPreKeys(account.id);
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        axolotlDao().setSignedPreKey(account, signedPreKeyId, record);
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        return axolotlDao().hasSignedPreKey(account.id, signedPreKeyId);
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        axolotlDao().markSignedPreKeyAsRemoved(account.id, signedPreKeyId);
    }
}
