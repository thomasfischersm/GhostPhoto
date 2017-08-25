package com.playposse.ghostphoto.util.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * An {@link ImageView} that sets the height to be equal the width.
 */
public class SquareImageView extends android.support.v7.widget.AppCompatImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            int width = getMeasuredWidth();
            //noinspection SuspiciousNameCombination
            setMeasuredDimension(width, width);
        } else {
            int height = getMeasuredHeight();
            //noinspection SuspiciousNameCombination
            setMeasuredDimension(height, height);
        }
    }
}
