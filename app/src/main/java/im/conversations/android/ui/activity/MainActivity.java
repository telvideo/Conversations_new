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

import android.content.Intent;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import im.conversations.android.R;
import im.conversations.android.databinding.ActivityMainBinding;
import im.conversations.android.service.ForegroundService;
import im.conversations.android.ui.model.MainViewModel;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);
        final ViewModelProvider viewModelProvider =
                new ViewModelProvider(this, getDefaultViewModelProviderFactory());
        final var mainViewModel = viewModelProvider.get(MainViewModel.class);
        mainViewModel
                .hasNoAccounts()
                .observe(
                        this,
                        hasNoAccounts -> {
                            if (Boolean.TRUE.equals(hasNoAccounts)) {
                                startActivity(new Intent(this, SetupActivity.class));
                                finish();
                            }
                        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ForegroundService.start(this);
    }
}
