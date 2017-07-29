package com.playposse.ghostphoto.activities.review;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playposse.ghostphoto.R;
import com.playposse.ghostphoto.data.GhostPhotoContract.PhotoTable;
import com.playposse.ghostphoto.util.SmartCursor;

/**
 * A {@link Fragment} that includes a {@link ViewPager}, which contains
 * {@link ViewPhotoIndividualFragment} to display photos. The user can use this fragment to page
 * through all the photos. A parameter sets which the initial photo is.
 */
public class ViewPhotoContainerFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String PHOTO_SHOOT_ID_PARAM = "photoShootId";
    private static final String INITIAL_PHOTO_ID_PARAM = "initialPhotoId";

    private static final int LOADER_ID = 7;

    private long photoShootId;
    private long initialPhotoId;

    private ViewPager viewPager;
    private PhotoShootPagerAdapter pagerAdapter;

    public ViewPhotoContainerFragment() {
        // Required empty public constructor
    }

    public static ViewPhotoContainerFragment newInstance(long photoShootId, long initialPhotoId) {
        ViewPhotoContainerFragment fragment = new ViewPhotoContainerFragment();
        Bundle args = new Bundle();
        args.putLong(PHOTO_SHOOT_ID_PARAM, photoShootId);
        args.putLong(INITIAL_PHOTO_ID_PARAM, initialPhotoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoShootId = getArguments().getLong(PHOTO_SHOOT_ID_PARAM);
            initialPhotoId = getArguments().getLong(INITIAL_PHOTO_ID_PARAM);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_photo_container, container, false);

        viewPager = (ViewPager) rootView;

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where = PhotoTable.SHOOT_ID_COLUMN + "=" + photoShootId;
        String orderBy = PhotoTable.CREATED_COLUMN + " asc";
        return new CursorLoader(
                getContext(),
                PhotoTable.CONTENT_URI,
                PhotoTable.COLUMN_NAMES,
                where,
                null,
                orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (pagerAdapter == null) {
            pagerAdapter = new PhotoShootPagerAdapter(getFragmentManager(), cursor);
            viewPager.setAdapter(pagerAdapter);
            pagerAdapter.moveToInitialPhoto();
        } else {
            pagerAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        pagerAdapter.swapCursor(null);
    }

    /**
     * A {@link FragmentStatePagerAdapter} that has one {@link Fragment} for each photo in the
     * photo shoot.
     */
    private class PhotoShootPagerAdapter extends FragmentStatePagerAdapter {

        private Cursor cursor;
        private SmartCursor smartCursor;

        private PhotoShootPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm);

            swapCursor(cursor);
        }

        private void swapCursor(Cursor cursor) {
            this.cursor = cursor;
            smartCursor = new SmartCursor(cursor, PhotoTable.COLUMN_NAMES);
            notifyDataSetChanged();
        }

        private void moveToInitialPhoto() {
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    long photoId = smartCursor.getLong(PhotoTable.ID_COLUMN);
                    if (initialPhotoId == photoId) {
                        viewPager.setCurrentItem(cursor.getPosition());
                        return;
                    }
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            long photoId = smartCursor.getLong(PhotoTable.ID_COLUMN);
            return ViewPhotoIndividualFragment.newInstance(photoId);
        }

        @Override
        public int getCount() {
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        }
    }
}
