package com.playposse.ghostphoto.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A utility for rotating {@link Bitmap}s.
 */
public class BitmapRotationUtil {

    private static final String LOG_TAG = BitmapRotationUtil.class.getSimpleName();

    public static final int LEFT_ROTATION_DEGREES = -90;
    public static final int RIGHT_ROTATION_DEGREES = 90;

    public static void rotate(File imageFile, int degrees, RotationCallback callback) {
        new RotateAsyncTask(imageFile, callback, degrees).execute();
    }

    private static File rotate(File sourceFile, int degrees) throws IOException {
        Log.d(LOG_TAG, "rotate: Rotating image: " + sourceFile.getAbsolutePath());

        // Load bitmap.
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sourceBitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), option);
        if (sourceBitmap == null) {
            Log.e(LOG_TAG, "rotate: Failed to load bitmap for " + sourceFile.getAbsolutePath());
            return null;
        }

        // Rotate bitmap.
        Bitmap resultBitmap = rotate(sourceBitmap, degrees);

        // Compute next file name.
        File resultFile = computeNextFileName(sourceFile);
        Log.d(LOG_TAG, "rotate: Saving rotated photo as "+ resultFile.getAbsolutePath());

        // Save bitmap.
        FileOutputStream outStream = new FileOutputStream(resultFile);
        try {
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
        } finally {
            outStream.close();
        }

        // Delete the old file.
        boolean deleteSuccess = sourceFile.delete();
        if (!deleteSuccess) {
            Log.e(LOG_TAG, "rotate: Failed to delete source file after rotating: "
                    + sourceFile.getAbsolutePath());
        }

        return resultFile;
    }

    /**
     * Computes a new file name.
     *
     * Glide has problems with invalidating the cache. So, rather than invalidating the cache, it's
     * easier to change the file name.
     */
    private static File computeNextFileName(File sourceFile) {
        String sourcePath = sourceFile.getAbsolutePath();
        String sourceFileName =
                sourcePath.substring(sourcePath.lastIndexOf("/"), sourcePath.lastIndexOf("."));
        int underscoreCount = StringUtil.countOccurrencesOf(sourceFileName, '_');

        if (underscoreCount == 3) {
            // The file has never been rotated.
            return new File(sourcePath.replaceAll(".jpg$", "_0.jpg"));
        } else {
            String lastCountStr = sourceFileName.substring(sourceFileName.lastIndexOf("_") + 1);
            int lastCount = Integer.parseInt(lastCountStr);
            int newCount = lastCount + 1;
            return new File(sourcePath.replaceAll("_\\d+.jpg$", "_" + newCount + ".jpg"));
        }
    }

    private static Bitmap rotate(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true);
    }

    /**
     * An {@link AsyncTask} that rotates a photo.
     */
    private static class RotateAsyncTask extends AsyncTask<Void, Void, File> {

        private final File imageFile;
        private final RotationCallback callback;
        private final int degrees;

        private RotateAsyncTask(File imageFile, RotationCallback callback, int degrees) {
            this.imageFile = imageFile;
            this.callback = callback;
            this.degrees = degrees;
        }

        @Override
        protected File doInBackground(Void... params) {
            try {
                return rotate(imageFile, degrees);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "doInBackground: Failed to rotate image.", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(File newFile) {
            callback.onRotated(newFile);
        }
    }

    /**
     * Callback that is triggered after a rotation.
     */
    public interface RotationCallback {

        void onRotated(File newFile);
    }
}
