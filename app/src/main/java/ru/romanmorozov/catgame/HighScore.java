package ru.romanmorozov.catgame;

import android.content.Context;
import android.provider.BaseColumns;

/**
 * Created by morozovr on 6/28/2016.
 */
public class HighScore implements Comparable<HighScore> {

    private String id;
    private String highscore;
    private String date;
    private Context context;

    public void setId(String id) {
        this.id = id;
    }

    public void setHighscore(String highscore) {
        this.highscore = highscore;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {

        return id;
    }

    public String getHighscore() {
        return highscore;
    }

    public String getDate() {
        return date;
    }

    public HighScore() {
    }

    public HighScore(String highscore, String date, Context context) {
        setHighscore(highscore);
        setDate(date);
        this.context = context;
    }

    public HighScore(String id, String highscore, String date, Context context) {
        setId(id);
        setHighscore(highscore);
        setDate(date);
        this.context = context;
    }

    /* Inner class that defines the table contents */
    public static abstract class HighScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "highscores";
        public static final String COLUMN_NAME_HIGHSCORE = "highscore";
        public static final String COLUMN_NAME_DATE = "date";
    }

    @Override
    public String toString() {
        return context.getString(R.string.hs_id) + id + context.getString(R.string.hs_highscore) + highscore + context.getString(R.string.hs_date) + date;
    }

    @Override
    public int compareTo(HighScore another) {
        return this.highscore.compareTo(another.getHighscore());
    }
}
