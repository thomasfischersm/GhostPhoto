package com.playposse.ghostphoto.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.playposse.ghostphoto.R;


/**
 * Utility methods for sending e-mail.
 */
public final class EmailUtil {

    private EmailUtil() {}

    public static void sendFeedbackAction(Context context) {
        showInfoDialogAndSendEmail(
                context,
                R.string.feedback_dialog_title,
                R.string.feedback_dialog_message,
                R.string.feedback_email_recipient,
                R.string.feedback_email_subject,
                context.getResources().getString(R.string.feedback_email_body));
    }

    /**
     * Shows the user a dialog that the e-mail client is about to be opened.
     */
    private static void showInfoDialogAndSendEmail(
            final Context context,
            int dialogTitle,
            int dialogMessage,
            final int recipient,
            final int subject,
            final String bodyString) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(context.getResources().getString(dialogTitle));
        dialogBuilder.setMessage(context.getResources().getString(dialogMessage));
        dialogBuilder.setPositiveButton(
                context.getResources().getString(R.string.dialog_continue_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendEmail(context, recipient, subject, bodyString);
                    }
                }
        );
        dialogBuilder.setNegativeButton(
                context.getResources().getString(R.string.dialog_cancel_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        dialogBuilder.create().show();
    }

    private static void sendEmail(Context context, int recipient, int subject, String bodyString) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getResources().getString(recipient)});
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(subject));
        intent.putExtra(Intent.EXTRA_TEXT, bodyString);
        context.startActivity(intent);
    }
}
