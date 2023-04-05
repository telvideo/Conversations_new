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

package im.conversations.android.ui.graphics.drawable;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;

public class FlashBackgroundDrawable extends AnimationDrawable {

    private final long messageId;

    private FlashBackgroundDrawable(final Context context, final long messageId) {
        this.messageId = messageId;
        @ColorInt
        int backgroundColor =
                MaterialColors.getColor(
                        context,
                        com.google.android.material.R.attr.colorSurfaceVariant,
                        "colorSurfaceVariant not found");
        for (int i = 0; i < 3; ++i) {
            this.addFrame(new ColorDrawable(backgroundColor), 250);
            this.addFrame(new ColorDrawable(android.graphics.Color.TRANSPARENT), 250);
        }
        this.setEnterFadeDuration(125);
        this.setExitFadeDuration(125);
        this.setOneShot(true);
    }

    public boolean needsReset(final long messageId) {
        return this.messageId != messageId || !this.isRunning();
    }

    public static void flashBackground(@NonNull final View view, final long messageId) {
        final var animationDrawable = new FlashBackgroundDrawable(view.getContext(), messageId);
        view.setBackground(animationDrawable);
        view.post(animationDrawable::start);
    }
}
