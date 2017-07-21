package com.playposse.ghostphoto.util.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * A helper class that creates grid spacing in a {@link RecyclerView}.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private static final String LOG_TAG = SpaceItemDecoration.class.getSimpleName();

    private final int space;

    public SpaceItemDecoration(Context context, int spaceResId) {
        this.space = (int) context.getResources().getDimension(spaceResId);
    }

    @Override
    public void getItemOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state) {

        int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
        int rowCount = (parent.getChildCount() / spanCount) + 1;
        int position = parent.getChildLayoutPosition(view);

        int[] horizontal = calculateSpacing(position, spanCount, space);
        int[] vertical = calculateSpacing(position / spanCount, rowCount, space);
        outRect.left = horizontal[0];
        outRect.right = horizontal[1];
        outRect.top = vertical[0];
        outRect.bottom = vertical[1];

        Log.i(LOG_TAG, "getItemOffsets: position: " + position + " left: " + horizontal[0]
                + " right: " + horizontal[1]);
    }

    private int[] calculateSpacing(int position, int max, int spacing) {
        position = position % max;
        double widthReduction = spacing * (max - 1.0) / max;
        double left = 0;
        double right = widthReduction - left;
        Log.i(LOG_TAG, "calculateSpacing: space: " + spacing + " widthReduction: " + widthReduction);
        Log.i(LOG_TAG, "calculateSpacing: left: " + left + " rigth: " + right);

        for (int i = 0; i < position; i++) {
            left = spacing - right;
            right = widthReduction - left;
            Log.i(LOG_TAG, "calculateSpacing: left: " + left + " rigth: " + right);
        }
        return new int[]{(int) left, (int) right};
    }
}
