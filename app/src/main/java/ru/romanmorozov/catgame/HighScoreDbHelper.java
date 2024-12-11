package ru.romanmorozov.catgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by morozovr on 6/28/2016.
 */
public class HighScoreDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    private static final int DATABASE_SIZE = 5;
    public static final String DATABASE_NAME = "HighScores4.db";

    private HighScoreDbHelper helper;
    private SQLiteDatabase db;

    private Context context;
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HighScore.HighScoreEntry.TABLE_NAME + " (" +
                    HighScore.HighScoreEntry._ID + " INTEGER PRIMARY KEY," +
                    HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE + TEXT_TYPE + COMMA_SEP +
                    HighScore.HighScoreEntry.COLUMN_NAME_DATE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HighScore.HighScoreEntry.TABLE_NAME;

    public HighScoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public ArrayList<HighScore> getHighscores() {
        helper = new HighScoreDbHelper(context);
        db = helper.getWritableDatabase();
        final ArrayList<HighScore> result = new ArrayList<HighScore>();

        String[] projection = {
                HighScore.HighScoreEntry._ID,
                HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE,
                HighScore.HighScoreEntry.COLUMN_NAME_DATE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE + " ASC";

        Cursor c = db.query(
                HighScore.HighScoreEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,//selection,                                // The columns for the WHERE clause
                null,//selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        try {
            while (c.moveToNext()) {
                result.add(
                        new HighScore(
                                c.getString(c.getColumnIndexOrThrow(HighScore.HighScoreEntry._ID)),
                                c.getString(c.getColumnIndexOrThrow(HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE)),
                                c.getString(c.getColumnIndexOrThrow(HighScore.HighScoreEntry.COLUMN_NAME_DATE)), context));
            }
        } finally {
            c.close();
        }
        return result;
    }

    public void saveHighScore(HighScore highScore) {
        helper = new HighScoreDbHelper(context);
        db = helper.getWritableDatabase();
        //Retrieving minimal highscore
        String[] projection = {
                HighScore.HighScoreEntry._ID,
                HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE,
                HighScore.HighScoreEntry.COLUMN_NAME_DATE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE + " ASC";
        // Define 'where' part of query.
        String selection = HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE + " > ?";

        String[] selectionArgs = new String[]{highScore.getHighscore()};

        Cursor c = db.query(
                HighScore.HighScoreEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,//selection,                                // The columns for the WHERE clause
                selectionArgs,//selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        if (getHighscores().size() < DATABASE_SIZE) {


            ContentValues values = new ContentValues();
            values.put(HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE, highScore.getHighscore());
            values.put(HighScore.HighScoreEntry.COLUMN_NAME_DATE, highScore.getDate());

            // Insert the new row, returning the primary key value of the new row
            db.insert(
                    HighScore.HighScoreEntry.TABLE_NAME,
                    null,
                    values);

        } else {

            if (c.getCount() > 0) {

                c.moveToFirst();

                long itemId = c.getLong(
                        c.getColumnIndexOrThrow(HighScore.HighScoreEntry._ID)
                );

                //Update the row
                // New value for one column
                ContentValues values = new ContentValues();
                values.put(HighScore.HighScoreEntry.COLUMN_NAME_HIGHSCORE, highScore.getHighscore());
                values.put(HighScore.HighScoreEntry.COLUMN_NAME_DATE, highScore.getDate());

// Which row to update, based on the ID
                selection = HighScore.HighScoreEntry._ID + " LIKE ?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                db.update(
                        HighScore.HighScoreEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
            }
        }
    }

    public void clearHighScores() {
        helper = new HighScoreDbHelper(context);
        db = helper.getWritableDatabase();
        db.delete(HighScore.HighScoreEntry.TABLE_NAME, null, null);
    }
}