package com.playposse.ghostphoto.activities.review;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.activities.ParentActivity;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoShootTable;
import com.playposse.ghostphoto.util.RecyclerViewCursorAdapter;
import com.playposse.ghostphoto.util.ResponsiveGridLayoutManager;
import com.playposse.ghostphoto.util.SmartCursor;

/**
 * An {@link Activity} that lists all of the photo shoots.
 */
public class ListShootsActivity extends ParentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ListShootsActivity.class.getSimpleName();

    private static final int LOADER_MANAGER = 1;

    private RecyclerView shootRecyclerView;

    private TextView loadingMessageTextView;
    private TextView emptyMessageTextView;
    private PhotoShootAdapter photoShootAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_shoots);

        initActionBar();

        shootRecyclerView = (RecyclerView) findViewById(R.id.shootRecyclerView);
        loadingMessageTextView = (TextView) findViewById(R.id.loadingMessageTextView);
        emptyMessageTextView = (TextView) findViewById(R.id.emptyMessageTextView);

        shootRecyclerView.setLayoutManager(
                new ResponsiveGridLayoutManager(this, R.dimen.cover_photo_min_column_width));
        photoShootAdapter = new PhotoShootAdapter();
        shootRecyclerView.setAdapter(photoShootAdapter);

        getLoaderManager().initLoader(LOADER_MANAGER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                PhotoShootTable.CONTENT_URI,
                PhotoShootTable.SELECT_COLUMN_NAMES,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        loadingMessageTextView.setVisibility(View.GONE);

        photoShootAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0) {
            shootRecyclerView.setVisibility(View.VISIBLE);
            emptyMessageTextView.setVisibility(View.GONE);
        } else {
            shootRecyclerView.setVisibility(View.GONE);
            emptyMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loadingMessageTextView.setVisibility(View.VISIBLE);
        shootRecyclerView.setVisibility(View.GONE);
        emptyMessageTextView.setVisibility(View.GONE);

        photoShootAdapter.swapCursor(null);
    }

    /**
     * An {@link Adapter} to show a list of photo shoots.
     */
    private class PhotoShootAdapter extends RecyclerViewCursorAdapter<PhotoShootViewHolder> {

        @Override
        public PhotoShootViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(
                    R.layout.photo_shoot_list_item,
                    parent,
                    false);
            return new PhotoShootViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(PhotoShootViewHolder holder, int position, Cursor cursor) {
            // Retrieve data.
            SmartCursor smartCursor = new SmartCursor(cursor, PhotoShootTable.SELECT_COLUMN_NAMES);
            final long photoShootId = smartCursor.getLong(PhotoShootTable.ID_COLUMN);
            String firstPhotoUriStr = smartCursor.getString(PhotoShootTable.FIRST_PHOTO_URI_COLUMN);
            int photoCount = smartCursor.getInt(PhotoShootTable.PHOTO_COUNT_COLUMN);

            // Prepare data.
            Uri firstPhotoUri = Uri.parse(firstPhotoUriStr);
            String photoCountStr = getString(R.string.photo_count_label, photoCount);

            // Update the UI.
            holder.getPhotoCountTextView().setText(photoCountStr);
            // TODO: Add time to UI
            Glide.with(getApplicationContext())
                    .load(firstPhotoUri)
                    .into(holder.getPhotoImageView());

            // Set event listeners
            holder.getPhotoImageView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Open comparison activity.
                    Log.d(LOG_TAG, "onClick: Open comparison for shoot" + photoShootId);
                }
            });
        }
    }

    /**
     * The {@link ViewHolder} that represents a photo shoot by showing its cover photo. The cover
     * photo is simply the first photo.
     */
    private class PhotoShootViewHolder extends ViewHolder {

        private final ImageView photoImageView;
        private final TextView photoCountTextView;

        private PhotoShootViewHolder(View view) {
            super(view);

            photoImageView = (ImageView) view.findViewById(R.id.photoImageView);
            photoCountTextView = (TextView) view.findViewById(R.id.photoCountTextView);
        }

        public ImageView getPhotoImageView() {
            return photoImageView;
        }

        public TextView getPhotoCountTextView() {
            return photoCountTextView;
        }
    }
}
