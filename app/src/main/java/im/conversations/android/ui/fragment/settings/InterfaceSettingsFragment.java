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
import com.google.android.material.color.DynamicColors;
import im.conversations.android.Conversations;
import im.conversations.android.R;
import im.conversations.android.ui.activity.SettingsActivity;

public class InterfaceSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_interface, rootKey);
        final var themePreference = findPreference("theme");
        final var dynamicColors = findPreference("dynamic_colors");
        if (themePreference == null || dynamicColors == null) {
            throw new IllegalStateException(
                    "The preference resource file did not contain theme or color preferences");
        }
        themePreference.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    if (newValue instanceof final String theme) {
                        final int desiredNightMode = Conversations.getDesiredNightMode(theme);
                        requireSettingsActivity().setDesiredNightMode(desiredNightMode);
                    }
                    return true;
                });
        dynamicColors.setVisible(DynamicColors.isDynamicColorAvailable());
        dynamicColors.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    requireSettingsActivity().setDynamicColors(Boolean.TRUE.equals(newValue));
                    return true;
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.pref_title_interface);
    }

    public SettingsActivity requireSettingsActivity() {
        final var activity = requireActivity();
        if (activity instanceof SettingsActivity) {
            return (SettingsActivity) activity;
        }
        throw new IllegalStateException(
                String.format(
                        "%s is not %s",
                        activity.getClass().getName(), SettingsActivity.class.getName()));
    }
}
