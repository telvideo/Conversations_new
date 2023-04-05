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

package im.conversations.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Rational;
import eu.siacs.conversations.Config;

public class SurfaceViewRenderer extends org.webrtc.SurfaceViewRenderer {

    private Rational aspectRatio = new Rational(1, 1);

    private OnAspectRatioChanged onAspectRatioChanged;

    public SurfaceViewRenderer(Context context) {
        super(context);
    }

    public SurfaceViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
        super.onFrameResolutionChanged(videoWidth, videoHeight, rotation);
        final int rotatedWidth = rotation != 0 && rotation != 180 ? videoHeight : videoWidth;
        final int rotatedHeight = rotation != 0 && rotation != 180 ? videoWidth : videoHeight;
        final Rational currentRational = this.aspectRatio;
        this.aspectRatio = new Rational(rotatedWidth, rotatedHeight);
        Log.d(
                Config.LOGTAG,
                "onFrameResolutionChanged("
                        + rotatedWidth
                        + ","
                        + rotatedHeight
                        + ","
                        + aspectRatio
                        + ")");
        if (currentRational.equals(this.aspectRatio) || onAspectRatioChanged == null) {
            return;
        }
        onAspectRatioChanged.onAspectRatioChanged(this.aspectRatio);
    }

    public void setOnAspectRatioChanged(final OnAspectRatioChanged onAspectRatioChanged) {
        this.onAspectRatioChanged = onAspectRatioChanged;
    }

    public Rational getAspectRatio() {
        return this.aspectRatio;
    }

    public interface OnAspectRatioChanged {
        void onAspectRatioChanged(final Rational rational);
    }
}
