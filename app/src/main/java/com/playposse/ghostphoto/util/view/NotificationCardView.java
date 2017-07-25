package com.playposse.ghostphoto.util.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.playposse.ghostphoto.GhostPhotoPreferences;
import com.playposse.ghostphoto.R;

/**
 * A {@link CardView} that shows a message and a dismiss link. The visibility of the
 * {@link CardView} is tied to a preference that remembers if the view has already been shown.
 */
public class NotificationCardView extends CardView {

    private String preferenceKey;

    public NotificationCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public NotificationCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Inflate layout.
        View view = inflate(getContext(), R.layout.notification_card_view, this);

        // Read attributes.
        TypedArray a = context
                .getTheme()
                .obtainStyledAttributes(attrs, R.styleable.NotificationCardView, 0, 0);
        String message = a.getString(R.styleable.NotificationCardView_message);
        preferenceKey = a.getString(R.styleable.NotificationCardView_preferenceKey);
        boolean shouldShow = GhostPhotoPreferences.getBoolean(context, preferenceKey, true);

        // Find child views.
        TextView messageTextView = (TextView) findViewById(R.id.messageTextView);
        TextView dismissLink = (TextView) findViewById(R.id.dismissLink);

        // Apply attributes.
        setVisibility(shouldShow ? VISIBLE : GONE);
        messageTextView.setText(message);
        dismissLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onDismissClicked();
            }
        });
    }

    private void onDismissClicked() {
        setVisibility(GONE);
        GhostPhotoPreferences.setBoolean(getContext(), preferenceKey, false);
    }
}
