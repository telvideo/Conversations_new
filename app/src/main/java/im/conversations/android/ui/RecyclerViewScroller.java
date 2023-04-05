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

import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.ref.WeakReference;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecyclerViewScroller {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecyclerViewScroller.class);

    private final RecyclerView recyclerView;

    public RecyclerViewScroller(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void scrollToPosition(final int position) {
        this.scrollToPosition(position, null);
    }

    public void scrollToPosition(final int position, final Runnable onScrolledRunnable) {
        final ReliableScroller reliableScroller = new ReliableScroller(recyclerView);
        reliableScroller.scrollToPosition(position, onScrolledRunnable);
    }

    private static class ReliableScroller {

        private static final int MAX_DELAY = 2000;
        private static final int INTERVAL = 50;

        private final WeakReference<RecyclerView> recyclerViewReference;

        private int accumulatedDelay = 0;

        private ReliableScroller(RecyclerView recyclerView) {
            this.recyclerViewReference = new WeakReference<>(recyclerView);
        }

        private void scrollToPosition(final int position, final Runnable onScrolledRunnable) {
            final var recyclerView = this.recyclerViewReference.get();
            if (recyclerView == null) {
                return;
            }
            final var isItemLoaded = isItemLoaded(recyclerView, position);
            final var isSurroundingRendered = isSurroundingRendered(recyclerView, position);
            final var doneUpdating = !recyclerView.hasPendingAdapterUpdates();
            final var viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            LOGGER.info(
                    "Item is loaded {}, isSurroundingRendered {}, doneUpdating {} accumulatedDelay"
                            + " {}",
                    isItemLoaded,
                    isSurroundingRendered,
                    doneUpdating,
                    accumulatedDelay);
            if ((isItemLoaded && isSurroundingRendered && doneUpdating && viewHolder != null)
                    || accumulatedDelay >= MAX_DELAY) {
                final var layoutManager = recyclerView.getLayoutManager();
                if (viewHolder != null && layoutManager instanceof LinearLayoutManager llm) {
                    final var child = viewHolder.itemView;
                    final int offset = recyclerView.getHeight() / 2 - child.getHeight() / 2;
                    LOGGER.info("scrollToPositionWithOffset({},{})", position, offset);
                    llm.scrollToPositionWithOffset(position, offset);
                } else {
                    LOGGER.info("scrollToPosition({})", position);
                    recyclerView.scrollToPosition(position);
                }
                if (onScrolledRunnable != null) {
                    recyclerView.post(onScrolledRunnable);
                }
                return;
            }
            recyclerView.scrollToPosition(position);
            accumulatedDelay += INTERVAL;
            recyclerView.postDelayed(
                    () -> {
                        scrollToPosition(position, onScrolledRunnable);
                    },
                    INTERVAL);
        }

        private static boolean isSurroundingRendered(
                final RecyclerView recyclerView, final int requestedPosition) {
            final var layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager llm) {
                final var first = llm.findFirstVisibleItemPosition();
                final var last = llm.findLastVisibleItemPosition();
                if (first == -1 || last == -1) {
                    return false;
                }
                final var isItemLoaded =
                        isItemLoaded(recyclerView, first) && isItemLoaded(recyclerView, last);
                if (isItemLoaded) {
                    final var requestedIsOnly =
                            first == requestedPosition && last == requestedPosition;
                    final var firstCompletelyVisible = llm.findFirstCompletelyVisibleItemPosition();
                    final var lastCompletelyVisible = llm.findLastCompletelyVisibleItemPosition();

                    final var requestedInRange =
                            firstCompletelyVisible <= requestedPosition
                                    && requestedPosition <= lastCompletelyVisible;
                    LOGGER.info(
                            "firstComp {} lastComp {} requested {} inRange {}",
                            firstCompletelyVisible,
                            lastCompletelyVisible,
                            requestedPosition,
                            requestedInRange);
                    return requestedIsOnly || requestedInRange;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        private static boolean isItemLoaded(final RecyclerView recyclerView, final int position) {
            final var adapter = recyclerView.getAdapter();
            if (adapter instanceof PagingDataAdapter<?, ?> pagingDataAdapter) {
                return Objects.nonNull(pagingDataAdapter.peek(position));
            } else {
                return true;
            }
        }
    }
}
