package com.playposse.ghostphoto.util;

import android.database.Cursor;
import android.net.Uri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An improvement over {@link Cursor} that returns values by column name.
 */
public class SmartCursor {

    private static final SimpleDateFormat iso8601Format =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Cursor cursor;
    private final Map<String, Integer> columnNameToIndexMap;

    public SmartCursor(Cursor cursor, String[] columnNames) {
        this.cursor = cursor;

        columnNameToIndexMap = new HashMap<>(columnNames.length);
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            columnNameToIndexMap.put(columnName, i);
        }
    }

    public String getString(String columnName) {
        return cursor.getString(columnNameToIndexMap.get(columnName));
    }

    public int getInt(String columnName) {
        return cursor.getInt(columnNameToIndexMap.get(columnName));
    }

    public double getDouble(String columnName) {
        return cursor.getDouble(columnNameToIndexMap.get(columnName));
    }

    public long getLong(String columnName) {
        return cursor.getLong(columnNameToIndexMap.get(columnName));
    }

    public int getInt(int columnIndex) {
        return cursor.getInt(columnIndex);
    }

    public boolean getBoolean(String columnName) {
        return getInt(columnName) > 0;
    }

    public Date getDate(String columnName) throws ParseException {
        return iso8601Format.parse(getString(columnName));
    }

    public Uri getUri(String columnName) {
        return Uri.parse(getString(columnName));
    }
}
