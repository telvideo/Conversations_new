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

package im.conversations.android.xmpp.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.ListenableFuture;
import im.conversations.android.AppSettings;
import im.conversations.android.tls.ScopeFingerprint;
import im.conversations.android.tls.TrustManagers;
import im.conversations.android.xmpp.XmppConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressLint("CustomX509TrustManager")
public class TrustManager extends AbstractManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrustManager.class);

    private final AppSettings appSettings;

    private Function<ScopeFingerprint, ListenableFuture<Boolean>> userInterfaceCallback;

    public TrustManager(final Context context, final XmppConnection connection) {
        super(context, connection);
        this.appSettings = new AppSettings(context);
    }

    private boolean isServerTrustedInSystemCaStore(
            final X509Certificate[] chain, final String authType) {
        final var trustManager = TrustManagers.getTrustManager();
        if (trustManager == null) {
            return false;
        }
        try {
            trustManager.checkServerTrusted(chain, authType);
            return true;
        } catch (final CertificateException e) {
            return false;
        }
    }

    public static String fingerprint(final byte[] bytes) {
        return fingerprint(bytes, bytes.length);
    }

    public static String fingerprint(final byte[] bytes, final int segments) {
        return Joiner.on('\n')
                .join(
                        Lists.transform(
                                Lists.transform(
                                        Lists.partition(Bytes.asList(bytes), segments),
                                        s -> Lists.transform(s, b -> String.format("%02X", b))),
                                hex -> Joiner.on(':').join(hex)));
    }

    public void setUserInterfaceCallback(
            final Function<ScopeFingerprint, ListenableFuture<Boolean>> callback) {
        this.userInterfaceCallback = callback;
    }

    public void removeUserInterfaceCallback(
            final Function<ScopeFingerprint, ListenableFuture<Boolean>> callback) {
        if (this.userInterfaceCallback == callback) {
            this.userInterfaceCallback = null;
        }
    }

    public X509TrustManager scopedTrustManager(final String scope) {
        return new ScopedTrustManager(scope);
    }

    private class ScopedTrustManager implements X509TrustManager {

        private final String scope;

        private ScopedTrustManager(final String scope) {
            this.scope = scope;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            throw new CertificateException(
                    "This implementation has no support for client certificates");
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            if (chain.length == 0) {
                throw new CertificateException("Certificate chain is zero length");
            }
            for (final X509Certificate certificate : chain) {
                certificate.checkValidity();
            }
            final boolean isTrustSystemCaStore = appSettings.isTrustSystemCAStore();
            if (isTrustSystemCaStore && isServerTrustedInSystemCaStore(chain, authType)) {
                return;
            }
            final X509Certificate certificate = chain[0];
            final byte[] fingerprint =
                    Hashing.sha256().hashBytes(certificate.getEncoded()).asBytes();
            final var scopeFingerprint = new ScopeFingerprint(scope, fingerprint);
            if (getDatabase().certificateTrustDao().isTrusted(getAccount(), scopeFingerprint)) {
                LOGGER.info("Found {} in database", scopeFingerprint);
                return;
            }
            final var callback = TrustManager.this.userInterfaceCallback;
            if (callback == null) {
                throw new CertificateException(
                        "No user interface registered. Can not trust certificate");
            }
            final ListenableFuture<Boolean> futureDecision = callback.apply(scopeFingerprint);
            final boolean decision;
            try {
                decision = Boolean.TRUE.equals(futureDecision.get(10, TimeUnit.SECONDS));
            } catch (final ExecutionException | InterruptedException | TimeoutException e) {
                futureDecision.cancel(true);
                throw new CertificateException(
                        "Timeout waiting for user response", Throwables.getRootCause(e));
            }
            if (decision) {
                return;
            }
            throw new CertificateException("User did not trust certificate");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
