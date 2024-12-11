package ru.romanmorozov.catgame;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by morozovr on 5/31/2016.
 */
public class CatGameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CatGameView"; // for logging errors
    private static final int DEFAULT_SPRITE_SIZE = 100;
    private static final int DEFAULT_SPRITE_COUNT = 5;


    private CatGameThread catGameThread; // controls the game loop
    private TextView timerTextView;
    private AppCompatActivity activity;
    private HighScoreDbHelper helper;
    private CountDownTimer timer;
    private boolean dialogIsDisplayed = false;


    private int screenWidth;
    private int screenHeight;
    private int angle;

    private float interval;
    private String time;

    private Bitmap background, bgrReverse;
    private int bgrScroll;
    private int bgrYSpeed; //Background scroll speed.
    private int bgrW;
    private int bgrH;

    //Create a flag for the onDraw method to alternate background with its mirror image.
    private boolean reverseBackgroundFirst = false;

    private AudioPlayer audioPlayer = new AudioPlayer();


    private ArrayList<Sprite> sprites;


    public CatGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);
        setZOrderOnTop(false);
        setBackgroundColor(Color.TRANSPARENT);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.brick_bgr); //Load a background.
        bgrScroll = 0;  //Background scroll position
        bgrYSpeed = 1; //Scrolling background speed

        activity = (AppCompatActivity) context; // store reference to MainActivity
        helper = new HighScoreDbHelper(getContext());

    }

    // called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder) {


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // called by surfaceChanged when the size of the SurfaceView changes,
    // such as when it's first added to the View hierarchy
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w; // store CatGameView's width
        screenHeight = h; // store CatGameView's height


        background = Bitmap.createScaledBitmap(background, w, h, true); //Scale background to fit the screen.
        bgrW = background.getWidth();
        bgrH = background.getHeight();

        Matrix matrix = new Matrix();  //Like a frame or mould for an image.
        matrix.setScale(-1, 1); //Horizontal mirror effect.
        bgrReverse = Bitmap.createBitmap(background, 0, 0, bgrW, bgrH, matrix, true); //Create a new mirrored bitmap by applying the matrix.

        if (!dialogIsDisplayed) {
            newGame();
        }
    }


    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        catGameThread.setRunning(false); // terminate cannonThread

        while (retry) {
            try {
                catGameThread.join(); // wait for cannonThread to finish
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    } // end method surfaceDestroyed

    public void newGame() {
        sprites = new ArrayList<Sprite>();
        CatGameHelper helper = new CatGameHelper();

        for (int i = 0; i < DEFAULT_SPRITE_COUNT; i++) {
            sprites.add(new Sprite(helper.getRandomNumberInRange(0, screenWidth - DEFAULT_SPRITE_SIZE),
                    DEFAULT_SPRITE_SIZE, helper.getRandomNumberInRange(10, 50), this, helper.getSpriteResource()));
        }
        catGameThread = new CatGameThread(getHolder()); // create thread
        catGameThread.setRunning(true); // start game running
        catGameThread.start(); // start the game loop thread

        timerTextView = (TextView) ((AppCompatActivity) getContext()).findViewById(R.id.countuptimer);
        timer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                String _millis = String.valueOf(60 - (millisUntilFinished / 1000));

                if (_millis.length() == 1) {
                    time = "00:0" + _millis;

                } else {
                    time = "00:" + _millis;

                }
                timerTextView.setText(time);
            }

            public void onFinish() {
                stopGame();
                showGameOverDialog(true);
            }

        };
        timer.start();
    }

    public String getFPS() {
        String result;
        if (interval != 0) {
            result = String.valueOf(Math.floor(1 / interval));
        } else result = "undefined";

        return "FPS: " + result;

    }

    private void updatePositions(double elapsedTimeMS) {
        interval = (float) (elapsedTimeMS / 1000.0); // convert to seconds

        //Next value for the background's position.
        if ((bgrScroll += bgrYSpeed) >= bgrW) {
            bgrScroll = 0;
            reverseBackgroundFirst = !reverseBackgroundFirst;
        }

        // Recalculation of sprites rotation
        angle += 5;
        if (angle > 360) {
            angle = 0;
        }


        synchronized (sprites) {
            for (Iterator<Sprite> it = sprites.iterator(); it.hasNext(); ) {
                Sprite sprite = it.next();

                if (sprite.getY() >= screenHeight - sprite.getSize() || sprite.getY() <= 0) {
                    sprite.setVerticalSpeed(sprite.getVerticalSpeed() * -1);
                }

                sprite.setY(sprite.getY() + sprite.getVerticalSpeed());
            }

        }
        if (sprites.size() == 0) {

            stopGame();
            showGameOverDialog(false);
        }

    }

    public void drawGameElements(Canvas canvas) {


        //Draw scrolling background.
        Rect fromRect1 = new Rect(0, 0, bgrW - bgrScroll, bgrH);
        Rect toRect1 = new Rect(bgrScroll, 0, bgrW, bgrH);

        Rect fromRect2 = new Rect(bgrW - bgrScroll, 0, bgrW, bgrH);
        Rect toRect2 = new Rect(0, 0, bgrScroll, bgrH);

        if (!reverseBackgroundFirst) {
            canvas.drawBitmap(background, fromRect1, toRect1, null);
            canvas.drawBitmap(bgrReverse, fromRect2, toRect2, null);
        } else {
            canvas.drawBitmap(background, fromRect2, toRect2, null);
            canvas.drawBitmap(bgrReverse, fromRect1, toRect1, null);
        }


        synchronized (sprites) {
            Iterator<Sprite> it = sprites.iterator();
            while (it.hasNext()) {
                Sprite sprite = it.next();
                canvas.save();
                canvas.rotate(angle, sprite.getX() + (sprite.getSize() / 2), sprite.getY() + (sprite.getSize() / 2));
                canvas.drawBitmap(sprite.getBitmap(), sprite.getX(), sprite.getY(), null);
                canvas.restore();
            }
        }
        canvas.drawText(getFPS(), screenWidth - 80, 20, new Paint(Color.BLACK));
    }

    // called when the user touches the screen in this Activity
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // get int representing the type of action which caused this event
        int action = e.getAction();

        // the user user touched the screen or dragged along the screen
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE) {
            synchronized (sprites) {
                for (Iterator<Sprite> it = sprites.iterator(); it.hasNext(); ) {
                    Sprite sprite = it.next();
                    if (sprite.getX() - sprite.getSize() <= e.getX() && e.getX() <= sprite.getX() + sprite.getSize()
                            && sprite.getY() - sprite.getSize() <= e.getY() && e.getY() <= sprite.getY() + sprite.getSize()) {
                        it.remove();
                        if (audioPlayer.isPlaying()) {
                            audioPlayer.stop();
                        } else {
                            audioPlayer.play(getContext(), R.raw.meow1);
                        }

                    }
                }
            }
        }


        return true;
    } // end method onTouchEvent

    private void stopGame() {
        catGameThread.setRunning(false);
        timer.cancel();
    }

    private boolean processHighScore(String time) {
        boolean result = false;
        HighScore highScore = new HighScore(time, DateFormat.getDateTimeInstance().format(new Date()), activity);
        ArrayList<HighScore> highScores = helper.getHighscores();

        if (highScores.isEmpty() || Collections.min(highScores).compareTo(highScore) > 0) {
            result = true;
        }
        helper.saveHighScore(highScore);

        return result;
    }


    private void showGameOverDialog(final boolean isLost) {
        final DialogFragment gameResult =
                new DialogFragment() {
                    // create an AlertDialog and return it
                    @Override
                    public Dialog onCreateDialog(Bundle bundle) {


                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        String title;
                        String message;

                        if (isLost) {
                            title = activity.getString(R.string.msg_game_over);

                        } else {
                            if (processHighScore(time)) {
                                title = activity.getString(R.string.msg_you_win) + time + activity.getString(R.string.msg_it_is_a_highscore);
                            } else {
                                title = activity.getString(R.string.msg_you_win) + time;
                            }
                        }

                        message = activity.getString(R.string.msg_play_again);

                        builder.setTitle(title);
                        builder.setMessage(message);
                        String positiveText = activity.getString(android.R.string.ok);
                        builder.setPositiveButton(positiveText,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialogIsDisplayed = false;
                                        newGame();
                                    }
                                });

                        String negativeText = activity.getString(android.R.string.cancel);
                        builder.setNegativeButton(negativeText,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        activity.finish();
                                        System.exit(0);
                                    }
                                });

                        return builder.create();

                    } // end method onCreateDialog
                }; // end DialogFragment anonymous inner class


        // display dialog
        activity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        dialogIsDisplayed = true;
                        gameResult.setCancelable(false); // modal dialog
                        gameResult.show(activity.getSupportFragmentManager(), "results");
                    }
                });
    }


    // Thread subclass to control the game loop
    private class CatGameThread extends Thread {
        private SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        // initializes the surface holder
        public CatGameThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CatGameThread");
        }

        // changes running state
        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        // controls the game loop
        @Override
        public void run() {
            Canvas canvas = null; // used for drawing
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas(null);

                    // lock the surfaceHolder for drawing
                    synchronized (surfaceHolder) {

                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        updatePositions(elapsedTimeMS); // update game state
                        if (canvas != null) {
                            drawGameElements(canvas); // draw using the canvas

                        }
                        previousFrameTime = currentTime; // update previous time
                    }

                } catch (Exception e) {
                } finally {
                    // display canvas's contents on the CatGameView
                    // and enable other threads to use the Canvas
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            } // end while
        } // end method run
    } // end nested class CatGameThread
} // end class CatGameView


