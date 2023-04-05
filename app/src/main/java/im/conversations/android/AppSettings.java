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

package im.conversations.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.preference.PreferenceManager;
import com.google.common.base.Strings;

public class AppSettings {

    public static final String PREFERENCE_KEY_RINGTONE = "call_ringtone";
    public static final String PREFERENCE_KEY_BTBV = "btbv";

    public static final String PREFERENCE_KEY_TRUST_SYSTEM_CA_STORE = "trust_system_ca_store";

    private final Context context;

    public AppSettings(final Context context) {
        this.context = context;
    }

    public Uri getRingtone() {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        final String incomingCallRingtone =
                sharedPreferences.getString(
                        PREFERENCE_KEY_RINGTONE,
                        context.getString(R.string.incoming_call_ringtone));
        return Strings.isNullOrEmpty(incomingCallRingtone) ? null : Uri.parse(incomingCallRingtone);
    }

    public void setRingtone(final Uri uri) {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(PREFERENCE_KEY_RINGTONE, uri == null ? null : uri.toString())
                .apply();
    }

    public boolean isBTBVEnabled() {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(
                PREFERENCE_KEY_BTBV, context.getResources().getBoolean(R.bool.btbv));
    }

    public boolean isTrustSystemCAStore() {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(
                PREFERENCE_KEY_TRUST_SYSTEM_CA_STORE,
                context.getResources().getBoolean(R.bool.btbv));
    }
}
