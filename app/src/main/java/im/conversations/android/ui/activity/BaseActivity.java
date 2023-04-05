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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import im.conversations.android.Conversations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseActivity.class);

    private Boolean isDynamicColors;

    @Override
    public void onStart() {
        super.onStart();
        final int desiredNightMode = Conversations.getDesiredNightMode(this);
        if (setDesiredNightMode(desiredNightMode)) {
            return;
        }
        final boolean isDynamicColors = Conversations.isDynamicColorsDesired(this);
        setDynamicColors(isDynamicColors);
    }

    public void setDynamicColors(final boolean isDynamicColors) {
        if (this.isDynamicColors == null) {
            this.isDynamicColors = isDynamicColors;
        } else {
            if (this.isDynamicColors != isDynamicColors) {
                LOGGER.info(
                        "Recreating {} because dynamic color setting has changed",
                        getClass().getSimpleName());
                recreate();
            }
        }
    }

    public boolean setDesiredNightMode(final int desiredNightMode) {
        if (desiredNightMode == AppCompatDelegate.getDefaultNightMode()) {
            return false;
        }
        AppCompatDelegate.setDefaultNightMode(desiredNightMode);
        LOGGER.info(
                "Recreating {} because desired night mode has changed", getClass().getSimpleName());
        recreate();
        return true;
    }
}
