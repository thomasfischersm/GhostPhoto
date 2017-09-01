package com.playposse.ghostphoto.util.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * A helper class that creates grid spacing in a {@link RecyclerView}.
 */
public class FixedSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private static final String LOG_TAG = FixedSpaceItemDecoration.class.getSimpleName();

    private final int space;

    public FixedSpaceItemDecoration(Context context, int spaceResId) {
        this.space = (int) context.getResources().getDimension(spaceResId);
    }

    @Override
    public void getItemOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);
        int gap = space / 2;

        outRect.left = (position == 0) ? 0 : gap;
        outRect.right = gap;
        outRect.top = gap;
        outRect.bottom = gap;
    }
}
