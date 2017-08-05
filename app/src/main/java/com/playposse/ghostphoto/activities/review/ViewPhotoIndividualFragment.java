package com.playposse.ghostphoto.activities.review;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.SmartCursor;

/**
 * A {@link Fragment} that shows a single photo.
 */
public class ViewPhotoIndividualFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ViewPhotoIndividualFragment.class.getSimpleName();

    private static final String PHOTO_ID_PARAM = "photoId";

    private static final int LOADER_ID = 8;

    private ImageView photoImageView;

    private long photoId;

    public static ViewPhotoIndividualFragment newInstance(long photoId) {
        ViewPhotoIndividualFragment fragment = new ViewPhotoIndividualFragment();
        Bundle args = new Bundle();
        args.putLong(PHOTO_ID_PARAM, photoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            photoId = getArguments().getLong(PHOTO_ID_PARAM);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_photo_individual, container, false);

        photoImageView = (ImageView) rootView.findViewById(R.id.photoImageView);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String whereClause = PhotoTable.ID_COLUMN + " = " + photoId;
        return new CursorLoader(
                getContext(),
                PhotoTable.CONTENT_URI,
                PhotoTable.COLUMN_NAMES,
                whereClause,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SmartCursor smartCursor = new SmartCursor(cursor, PhotoTable.COLUMN_NAMES);

        if (cursor.moveToFirst()) {
            String photoUri = smartCursor.getString(PhotoTable.FILE_URI_COLUMN);
            Uri contentUri = Uri.parse(photoUri);

            Glide.with(this)
                    .load(contentUri)
                    .apply(RequestOptions.noTransformation())
                    .into(photoImageView);
        } else {
            Log.e(LOG_TAG, "onLoadFinished: Failed to load photo cursor");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        photoImageView.setImageBitmap(null);
    }
}
