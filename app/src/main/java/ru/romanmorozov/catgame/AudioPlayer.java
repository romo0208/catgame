package ru.romanmorozov.catgame;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by MorozovR on 6/20/2016.
 */
public class AudioPlayer {

    private MediaPlayer mPlayer;


    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


    public void play(Context c, int resource) {

        stop();

        mPlayer = MediaPlayer.create(c, resource);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });

        mPlayer.start();
    }

    public boolean isPlaying() {
        return mPlayer != null;
    }

}

