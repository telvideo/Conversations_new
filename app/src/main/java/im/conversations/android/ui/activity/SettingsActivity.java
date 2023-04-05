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

package im.conversations.android.ui.activity;

import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import im.conversations.android.R;
import im.conversations.android.databinding.ActivitySettingsBinding;
import im.conversations.android.service.ForegroundService;
import im.conversations.android.ui.Activities;
import im.conversations.android.ui.fragment.settings.MainSettingsFragment;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySettingsBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_settings);
        setSupportActionBar(binding.materialToolbar);
        Activities.setStatusAndNavigationBarColors(this, binding.getRoot(), true);

        final var fragmentManager = getSupportFragmentManager();
        final var currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MainSettingsFragment())
                    .commit();
        }
        binding.materialToolbar.setNavigationOnClickListener(
                view -> {
                    if (fragmentManager.getBackStackEntryCount() == 0) {
                        finish();
                    } else {
                        fragmentManager.popBackStack();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        ForegroundService.start(this);
    }
}
