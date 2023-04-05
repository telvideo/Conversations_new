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

package im.conversations.android.ui;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public final class NavControllers {

    private NavControllers() {}

    public static NavController findNavController(
            final FragmentActivity activity, @IdRes int fragmentId) {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentById(fragmentId);
        if (fragment instanceof NavHostFragment) {
            return ((NavHostFragment) fragment).getNavController();
        }
        throw new IllegalStateException("Fragment was not of type NavHostFragment");
    }
}
