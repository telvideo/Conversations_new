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

package im.conversations.android.ui.fragment.settings;

import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import im.conversations.android.AppSettings;
import im.conversations.android.R;
import im.conversations.android.ui.activity.result.PickRingtone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationsSettingsFragment extends PreferenceFragmentCompat {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotificationsSettingsFragment.class);

    private final ActivityResultLauncher<Uri> pickRingtoneLauncher =
            registerForActivityResult(
                    new PickRingtone(),
                    result -> {
                        if (result == null) {
                            // do nothing. user aborted
                            return;
                        }
                        final Uri uri = PickRingtone.noneToNull(result);
                        setRingtone(uri);
                        LOGGER.info("User set ringtone to {}", uri);
                    });

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_notifications, rootKey);
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.notifications);
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        if (AppSettings.PREFERENCE_KEY_RINGTONE.equals(preference.getKey())) {
            pickRingtone();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void pickRingtone() {
        final Uri uri = appSettings().getRingtone();
        LOGGER.info("current ringtone {}", uri);
        this.pickRingtoneLauncher.launch(uri);
    }

    private void setRingtone(final Uri uri) {
        appSettings().setRingtone(uri);
    }

    private AppSettings appSettings() {
        return new AppSettings(requireContext());
    }
}
