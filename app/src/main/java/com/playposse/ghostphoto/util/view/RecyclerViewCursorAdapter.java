package com.playposse.ghostphoto.util.view;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;

/**
 * A cursor adapter for {@link Adapter}. Apparently, Android oversaw providing this.
 */
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends Adapter<VH> {

    private static final String LOG_TAG = RecyclerViewCursorAdapter.class.getSimpleName();

    private final DataSetObserver dataSetObserver = new SimpleDataSetObserver();

    private Cursor cursor;
    private Integer idColumnIndex;

    public RecyclerViewCursorAdapter() {
        setHasStableIds(true);
    }

    protected abstract void onBindViewHolder(VH holder, int position, Cursor cursor);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (cursor == null) {
            throw new IllegalStateException("Cursor should be valid.");
        }

        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid position: " + position);
        }

        onBindViewHolder(holder, position, cursor);
    }

    @Override
    public long getItemId(int position) {
        if ((cursor != null) && (idColumnIndex != null) && (cursor.moveToPosition(position))) {
            return cursor.getInt(idColumnIndex);
        }

        return RecyclerView.NO_ID;
    }

    protected Cursor getCursor(int position) {
        if ((cursor != null) && cursor.moveToPosition(position)) {
            return cursor;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        int count = (cursor != null) ? cursor.getCount() : 0;
        Log.i(LOG_TAG, "getItemCount: Called with item count " + count);
        return count;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.unregisterDataSetObserver(dataSetObserver);
        }

        cursor = newCursor;
        if (cursor != null) {
            cursor.registerDataSetObserver(dataSetObserver);
            idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
        } else {
            idColumnIndex = null;
        }

        notifyDataSetChanged();
    }

    /**
     * A simple {@link DataSetObserver} implementation.
     */
    private class SimpleDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetChanged();
        }
    }
}
