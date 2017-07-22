package com.playposse.ghostphoto.util.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.playposse.ghostphoto.R;

import java.util.List;


/**
 * A utility for creating simple dialogs.
 */
public final class DialogUtil {

    private static final String LOG_TAG = DialogUtil.class.getSimpleName();

    private DialogUtil() {
    }

    public static void showMultiChoiceDialog(
            Context context,
            int titleResId,
            int arrayResId,
            final List<Runnable> actionList) {

        new AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setItems(
                        arrayResId,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                if ((which < 0) || (which >= actionList.size())) {
                                    Log.e(LOG_TAG, "onClick: Multiple choice dialog received an " +
                                            "unexpected choice: " + which);
                                    return;
                                }

                                actionList.get(which).run();
                            }
                        })
                .setNegativeButton(
                        R.string.cancel_button_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();

    }
}
