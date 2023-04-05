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

package eu.siacs.conversations.xmpp.jingle;

import android.util.Log;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import eu.siacs.conversations.Config;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;

class TrackWrapper<T extends MediaStreamTrack> {
    public final T track;
    public final RtpSender rtpSender;

    private TrackWrapper(final T track, final RtpSender rtpSender) {
        Preconditions.checkNotNull(track);
        Preconditions.checkNotNull(rtpSender);
        this.track = track;
        this.rtpSender = rtpSender;
    }

    public static <T extends MediaStreamTrack> TrackWrapper<T> addTrack(
            final PeerConnection peerConnection, final T mediaStreamTrack) {
        final RtpSender rtpSender = peerConnection.addTrack(mediaStreamTrack);
        return new TrackWrapper<>(mediaStreamTrack, rtpSender);
    }

    public static <T extends MediaStreamTrack> Optional<T> get(
            @Nullable final PeerConnection peerConnection, final TrackWrapper<T> trackWrapper) {
        if (trackWrapper == null) {
            return Optional.absent();
        }
        final RtpTransceiver transceiver =
                peerConnection == null ? null : getTransceiver(peerConnection, trackWrapper);
        if (transceiver == null) {
            Log.w(
                    Config.LOGTAG,
                    "unable to detect transceiver for " + trackWrapper.getRtpSenderId());
            return Optional.of(trackWrapper.track);
        }
        final RtpTransceiver.RtpTransceiverDirection direction = transceiver.getDirection();
        if (direction == RtpTransceiver.RtpTransceiverDirection.SEND_ONLY
                || direction == RtpTransceiver.RtpTransceiverDirection.SEND_RECV) {
            return Optional.of(trackWrapper.track);
        } else {
            Log.d(Config.LOGTAG, "withholding track because transceiver is " + direction);
            return Optional.absent();
        }
    }

    public String getRtpSenderId() {
        try {
            return track.id();
        } catch (final IllegalStateException e) {
            return null;
        }
    }

    public static <T extends MediaStreamTrack> RtpTransceiver getTransceiver(
            @Nonnull final PeerConnection peerConnection, final TrackWrapper<T> trackWrapper) {
        final String rtpSenderId = trackWrapper.getRtpSenderId();
        if (rtpSenderId == null) {
            return null;
        }
        for (final RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            if (transceiver.getSender().id().equals(rtpSenderId)) {
                return transceiver;
            }
        }
        return null;
    }

    public static String id(final Class<? extends MediaStreamTrack> clazz) {
        return String.format(
                "%s-%s",
                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, clazz.getSimpleName()),
                UUID.randomUUID().toString());
    }
}
