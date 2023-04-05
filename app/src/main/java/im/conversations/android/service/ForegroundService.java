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

package im.conversations.android.service;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import im.conversations.android.notification.ForegroundServiceNotification;
import im.conversations.android.notification.RtpSessionNotification;
import im.conversations.android.xmpp.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForegroundService extends LifecycleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForegroundService.class);

    private final ForegroundServiceNotification foregroundServiceNotification =
            new ForegroundServiceNotification(this);

    @Override
    public void onCreate() {
        super.onCreate();
        final var pool = ConnectionPool.getInstance(this);
        startForeground(
                ForegroundServiceNotification.ID,
                foregroundServiceNotification.build(pool.buildSummary()));
        pool.setSummaryProcessor(this::onSummaryUpdated);
    }

    private void onSummaryUpdated(final ConnectionPool.Summary summary) {
        foregroundServiceNotification.update(summary);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOGGER.debug("Destroying service. Removing listeners");
        ConnectionPool.getInstance(this).setSummaryProcessor(null);
    }

    public static void start(final Context context) {
        if (RtpSessionNotification.isShowingOngoingCallNotification(context)) {
            LOGGER.info("Do not start foreground service. Ongoing call.");
            return;
        }
        startForegroundService(context);
    }

    static void startForegroundService(final Context context) {
        try {
            ContextCompat.startForegroundService(
                    context, new Intent(context, ForegroundService.class));
        } catch (final RuntimeException e) {
            LOGGER.error("Could not start foreground service", e);
        }
    }

    public static void stop(final Context context) {
        final var intent = new Intent(context, ForegroundService.class);
        context.stopService(intent);
    }
}
