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

package im.conversations.android.notification;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import im.conversations.android.AppSettings;
import im.conversations.android.R;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractNotification {

    protected static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
            Executors.newSingleThreadScheduledExecutor();

    protected final Context context;
    protected final AppSettings appSettings;

    protected AbstractNotification(final Context context) {
        this.context = context;
        this.appSettings = new AppSettings(context);
    }

    public boolean notificationsFromStrangers() {
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(
                "notifications_from_strangers",
                context.getResources().getBoolean(R.bool.notifications_from_strangers));
    }
}
