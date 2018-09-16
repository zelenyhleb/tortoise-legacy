package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

class SQLiteProcessor {

    private final SQLiteDatabase db;

    SQLiteProcessor(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    private void writeComposition(OldTrack track) {
        if (!readCompositions().contains(track)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.COMPOSITION_AUTHOR, track.getArtist());
            contentValues.put(Constants.COMPOSITION_NAME, track.getName());
            contentValues.put(Constants.COMPOSITION_PATH, track.getPath());
            contentValues.put(Constants.COMPOSITION_DURATION, track.getDuration());

            db.insert(Constants.COMPOSITIONS_LIST, null, contentValues);
        }

    }

    void writeCompositions(List<OldTrack> tracks) {
        for (OldTrack track : tracks) {
            writeComposition(track);
        }
    }

    void clearDatabase() {
        db.execSQL("delete from " + Constants.COMPOSITIONS_LIST);
    }

    List<OldTrack> readCompositions() {
        Cursor c = db.query(Constants.COMPOSITIONS_LIST, null, null, null, null, null, Constants.COMPOSITION_IDENTIFIER);
        List<OldTrack> tracks = new ArrayList<>();
        if (c.moveToFirst()) {
            int compositionIdColIndex = c.getColumnIndex(Constants.COMPOSITION_IDENTIFIER);
            int compositionAuthorColIndex = c.getColumnIndex(Constants.COMPOSITION_AUTHOR);
            int compositionNameColIndex = c.getColumnIndex(Constants.COMPOSITION_NAME);
            int compositionPathColIndex = c.getColumnIndex(Constants.COMPOSITION_PATH);
            int compositionDurationColIndex = c.getColumnIndex(Constants.COMPOSITION_DURATION);

            do {
                String duration = c.getString(compositionDurationColIndex);
                String author = c.getString(compositionAuthorColIndex);
                String name = c.getString(compositionNameColIndex);
                String path = c.getString(compositionPathColIndex);
                int id = c.getInt(compositionIdColIndex);
                tracks.add(new OldTrack(duration, author, name, path, id));

            } while (c.moveToNext());
        }
        c.close();
        return tracks;
    }
}
