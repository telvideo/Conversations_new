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

package im.conversations.android.ui.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import im.conversations.android.xmpp.ConnectionPool;
import im.conversations.android.xmpp.manager.JingleConnectionManager;
import java.util.function.Consumer;

public class RtpSessionViewModel extends AndroidViewModel {

    private long accountId;

    public RtpSessionViewModel(@NonNull Application application) {
        super(application);
    }

    public void setAccountId(
            final long accountId, final Consumer<JingleConnectionManager> jmcConsumer) {
        this.accountId = accountId;
        this.connectJingleConnectionManager(accountId, jmcConsumer);
    }

    private void connectJingleConnectionManager(
            long accountId, final Consumer<JingleConnectionManager> jmcConsumer) {
        final var connectionFuture = ConnectionPool.getInstance(getApplication()).get(accountId);
        final var jcmFuture =
                Futures.transform(
                        connectionFuture,
                        connection -> connection.getManager(JingleConnectionManager.class),
                        MoreExecutors.directExecutor());
        Futures.addCallback(
                jcmFuture,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(JingleConnectionManager manager) {
                        jmcConsumer.accept(manager);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // TODO show warning in activity
                    }
                },
                MoreExecutors.directExecutor());
    }
}
