package com.playposse.ghostphoto.util.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * A {@link GridLayoutManager} that adjusts the column count responding to the available size. It
 * takes a minItemWidth and maxColumnCount to decided on the best column count.
 */
public class ResponsiveGridLayoutManager extends GridLayoutManager {

    private final int width;

    private int lastWidth = -1;

    public ResponsiveGridLayoutManager(Context context, int minItemWidthResId) {
        super(context, 1 /* meaningless placeholder */);

        width = (int) context.getResources().getDimension(minItemWidthResId);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (lastWidth != getWidth()) {
            int availableWidth = getWidth() - getPaddingRight() - getPaddingLeft();
            int spanCount = Math.max(1, availableWidth / width);
            setSpanCount(spanCount);

            // Remember to avoid recalculating unnecessarily.
            lastWidth = getWidth();
        }

        super.onLayoutChildren(recycler, state);
    }
}
