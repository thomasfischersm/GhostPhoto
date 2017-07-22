package com.playposse.ghostphoto.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * A little helper class to send toasts when outside of an activity.
 */
public class ToastUtil {

    public static void sendToast(final Context context, final String message) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void sendToast(final Context context, final int resId) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void sendToast(
            final Context context,
            final int duration,
            final int resId,
            final Object... args) {

        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                String msg = String.format(context.getString(resId), args);
                Toast.makeText(context, msg, duration).show();
            }
        });
    }

    public static void sendShortToast(final Context context, final int resId) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
