package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.view.ViewCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.ExtraConstants;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.IntegrationUtil;
import com.playposse.ghostphoto.util.SmartCursor;
import com.playposse.ghostphoto.util.view.RecyclerViewCursorAdapter;
import com.playposse.ghostphoto.util.view.SpaceItemDecoration;

import java.io.File;
import java.net.URISyntaxException;

import static android.support.v7.widget.RecyclerView.VERTICAL;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * An {@link Activity} that shows the photos in a photo shoot and lets the user pick photos to keep.
 */
public class ReviewPhotoShootActivity extends ParentActivity {

    private static final String LOG_TAG = ReviewPhotoShootActivity.class.getSimpleName();

    private static final int ALL_PHOTO_LADER = 3;
    private static final int SELECTED_PHOTO_LADER = 4;


    private RecyclerView allPhotosRecyclerView;
    private RecyclerView selectedPhotosRecyclerView;
    private TextView selectedPhotosHintTextView;

    private PhotoAdapter allPhotosAdapter;
    private PhotoAdapter selectedPhotosAdapter;

    private long photoShootIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review_photo_shoot);

        allPhotosRecyclerView = (RecyclerView) findViewById(R.id.allPhotosRecyclerView);
        selectedPhotosRecyclerView = (RecyclerView) findViewById(R.id.selectedPhotosRecyclerView);
        selectedPhotosHintTextView = (TextView) findViewById(R.id.selectedPhotosHintTextView);

        initActionBar();

        photoShootIndex = ExtraConstants.getPhotoShootIndex(getIntent());

        PhotoDragListener photoDragListener = new PhotoDragListener();

        // Build allPhotosRecyclerView.
        GridLayoutManager allPhotosLayoutManager =
                new GridLayoutManager(this, 3, VERTICAL, false);
        allPhotosRecyclerView.setLayoutManager(allPhotosLayoutManager);
        allPhotosAdapter = new PhotoAdapter();
        allPhotosRecyclerView.setAdapter(allPhotosAdapter);
        allPhotosRecyclerView.addItemDecoration(
                new SpaceItemDecoration(this, R.dimen.photo_shoot_spacing));
        allPhotosRecyclerView.setOnDragListener(photoDragListener);

        // Build selectedPhotosRecyclerView.
        GridLayoutManager selectedPhotosLayoutManager =
                new GridLayoutManager(this, 1, VERTICAL, false);
        selectedPhotosRecyclerView.setLayoutManager(selectedPhotosLayoutManager);
        selectedPhotosAdapter = new PhotoAdapter();
        selectedPhotosRecyclerView.setAdapter(selectedPhotosAdapter);
        selectedPhotosRecyclerView.addItemDecoration(
                new SpaceItemDecoration(this, R.dimen.photo_shoot_spacing));
        selectedPhotosRecyclerView.setOnDragListener(photoDragListener);

        selectedPhotosHintTextView.setOnDragListener(photoDragListener);

        getLoaderManager().initLoader(ALL_PHOTO_LADER, null, new AllPhotoLoader());
        getLoaderManager().initLoader(SELECTED_PHOTO_LADER, null, new SelectedPhotoLoader());
    }

    /**
     * A {@link LoaderCallbacks} that loads all the photos in a photo shoot.
     */
    private class AllPhotoLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getApplicationContext(),
                    PhotoTable.CONTENT_URI,
                    PhotoTable.COLUMN_NAMES,
                    PhotoTable.SHOOT_ID_COLUMN + " = " + photoShootIndex,
                    null,
                    PhotoTable.ID_COLUMN + " asc");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            allPhotosAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            allPhotosAdapter.swapCursor(null);
        }
    }

    /**
     * A {@link LoaderCallbacks} that loads only the selected photos in a photo shoot.
     */
    private class SelectedPhotoLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String whereClause = PhotoTable.SHOOT_ID_COLUMN + " = " + photoShootIndex
                    + " and " + PhotoTable.IS_SELECTED_COLUMN + " = 1";
            return new CursorLoader(
                    getApplicationContext(),
                    PhotoTable.CONTENT_URI,
                    PhotoTable.COLUMN_NAMES,
                    whereClause,
                    null,
                    PhotoTable.ID_COLUMN + " asc");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            selectedPhotosAdapter.swapCursor(cursor);

            if (cursor.getCount() > 0) {
                selectedPhotosRecyclerView.setVisibility(View.VISIBLE);
                selectedPhotosHintTextView.setVisibility(View.GONE);
            } else {
                selectedPhotosRecyclerView.setVisibility(View.GONE);
                selectedPhotosHintTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            selectedPhotosAdapter.swapCursor(null);
            selectedPhotosRecyclerView.setVisibility(View.GONE);
            selectedPhotosHintTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A {@link RecyclerView.Adapter} that shows a photo.
     */
    private class PhotoAdapter extends RecyclerViewCursorAdapter<PhotoViewHolder> {

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(
                    R.layout.photo_list_item,
                    parent,
                    false);
            return new PhotoViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(PhotoViewHolder holder, int position, Cursor cursor) {
            SmartCursor smartCursor = new SmartCursor(cursor, PhotoTable.COLUMN_NAMES);
            final String photoUri = smartCursor.getString(PhotoTable.FILE_URI_COLUMN);
            final long photoId = smartCursor.getLong(PhotoTable.ID_COLUMN);
            Uri contentUri = Uri.parse(photoUri);

            final ImageView photoImageView = holder.getPhotoImageView();
            Glide.with(getApplicationContext())
                    .load(contentUri)
                    .into(photoImageView);

            // View individual photo.
            photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        File file = new File(new java.net.URI(photoUri));
                        Context context = getApplicationContext();
                        IntegrationUtil.openExternalActivityToShowPhoto(context, file);
                    } catch (URISyntaxException ex) {
                        Log.e(LOG_TAG, "onClick: Failed to open external photo viewer", ex);
                    }
                }
            });

            // Compare two photos.

            // Drag photo to selected or unselected.
            photoImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipData dragData = ClipData.newPlainText("", Long.toString(photoId));
                    View.DragShadowBuilder dragShadowBuilder =
                            new View.DragShadowBuilder(photoImageView);
                    ViewCompat.startDragAndDrop(
                            photoImageView,
                            dragData,
                            dragShadowBuilder,
                            null,
                            0);

                    return true;
                }
            });
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} for a photo.
     */
    private class PhotoViewHolder extends ViewHolder {

        private final ImageView photoImageView;

        private PhotoViewHolder(View itemView) {
            super(itemView);

            photoImageView = (ImageView) itemView.findViewById(R.id.photoImageView);
        }

        private ImageView getPhotoImageView() {
            return photoImageView;
        }
    }

    /**
     * A {@link View.OnDragListener} that listens for photos dragged to/from all/selected photos.
     */
    private class PhotoDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View view, DragEvent event) {
            final boolean isSelectedPhotosTarget;
            if (view == allPhotosRecyclerView) {
                isSelectedPhotosTarget = false;
            } else if ((view == selectedPhotosRecyclerView)
                    || (view == selectedPhotosHintTextView)) {
                isSelectedPhotosTarget = true;
            } else {
                Log.e(LOG_TAG, "onDrag: Unexpected view " + view);
                return false;
            }

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setBackgroundColor(getAvailableDragTargetTint());
                    view.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    view.setBackgroundColor(getActiveDragTargetTint());
                    view.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    // Ignore the event
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    view.setBackgroundColor(getAvailableDragTargetTint());
                    view.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    String photoIdStr =
                            event.getClipData().getItemAt(0).getText().toString();
                    long photoId = Long.parseLong(photoIdStr);
                    new SelectPhotoAsyncTask(photoId, isSelectedPhotosTarget).execute();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    view.setBackgroundColor(0);
                    view.invalidate();
                    return true;
                default:
                    Log.e(LOG_TAG, "onDrag: Unknown drag action type: " + event.getAction());
                    return false;
            }
        }

        private int getAvailableDragTargetTint() {
            return ContextCompat.getColor(getApplicationContext(), R.color.availableDragTargetTint);
        }

        private int getActiveDragTargetTint() {
            return ContextCompat.getColor(getApplicationContext(), R.color.activeDragTargetTint);
        }
    }

    /**
     * An {@link AsyncTask} that selects or deselects a photo.
     */
    private class SelectPhotoAsyncTask extends AsyncTask<Void, Void, Void> {

        private final long photoId;
        private final boolean isSelected;

        private SelectPhotoAsyncTask(long photoId, boolean isSelected) {
            this.photoId = photoId;
            this.isSelected = isSelected;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PhotoTable.IS_SELECTED_COLUMN, isSelected);

            String whereClause = PhotoTable.ID_COLUMN + " = " + photoId;

            getContentResolver().update(PhotoTable.CONTENT_URI, contentValues, whereClause, null);
            return null;
        }
    }
}
