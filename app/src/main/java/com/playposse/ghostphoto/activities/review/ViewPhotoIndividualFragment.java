package com.playposse.ghostphoto.activities.review;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.data.GhostPhotoContract;
import com.playposse.ghostphoto.util.SmartCursor;

import java.io.File;
import java.net.URISyntaxException;

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
    private Boolean isSelected = null;
    private File photoFile = null;

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

        getActivity().getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String whereClause = GhostPhotoContract.PhotoTable.ID_COLUMN + " = " + photoId;
        return new CursorLoader(
                getContext(),
                GhostPhotoContract.PhotoTable.CONTENT_URI,
                GhostPhotoContract.PhotoTable.COLUMN_NAMES,
                whereClause,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        SmartCursor smartCursor = new SmartCursor(cursor, GhostPhotoContract.PhotoTable.COLUMN_NAMES);

        if (cursor.moveToFirst()) {
            String photoUri = smartCursor.getString(GhostPhotoContract.PhotoTable.FILE_URI_COLUMN);
            isSelected = smartCursor.getBoolean(GhostPhotoContract.PhotoTable.IS_SELECTED_COLUMN);

            Uri contentUri = Uri.parse(photoUri);
            Glide.with(this)
                    .load(contentUri)
                    .into(photoImageView);

//            if (isSelected) {
//                selectButton.setImageResource(R.drawable.ic_select_off);
//            } else {
//                selectButton.setImageResource(R.drawable.ic_select);
//            }

            try {
                photoFile = new File(new java.net.URI(photoUri));
            } catch (URISyntaxException ex) {
                Log.d(LOG_TAG, "onLoadFinished: Failed to create photo file for editing.", ex);
            }
        } else {
            Log.e(LOG_TAG, "onLoadFinished: Failed to load photo cursor");
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        photoImageView.setImageBitmap(null);
    }
}
