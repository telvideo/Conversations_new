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

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import im.conversations.android.BuildConfig;
import im.conversations.android.R;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
        final var about = findPreference("about");
        if (about == null) {
            throw new IllegalStateException("The preference resource file did not 'about'");
        }
        about.setTitle(getString(R.string.title_activity_about_x, BuildConfig.APP_NAME));
        about.setSummary(String.format("%s %s", BuildConfig.APP_NAME, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.title_activity_settings);
    }
}
