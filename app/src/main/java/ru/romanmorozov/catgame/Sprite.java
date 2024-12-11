package ru.romanmorozov.catgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.SurfaceView;


/**
 * Created by MorozovR on 6/9/2016.
 */
public class Sprite {

    private int mSize;
    private int mX;
    private int mY;
    private float mVerticalSpeed;
    Bitmap mBitmap;

    public Sprite(int x, int y, int verticalSpeed, SurfaceView view, int resource) {

        mX = x;
        mY = y;
        mVerticalSpeed = verticalSpeed;
        mBitmap = BitmapFactory.decodeResource(view.getResources(), resource);
        mSize = mBitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public int getX() {
        return mX;
    }

    public void setX(int mX) {
        this.mX = mX;
    }

    public int getSize() {
        return mSize;
    }

    public int getY() {
        return mY;
    }

    public void setY(int mY) {
        this.mY = mY;
    }

    public int getVerticalSpeed() {
        return Math.round(mVerticalSpeed);
    }

    public void setVerticalSpeed(float mVerticalSpeed) {
        this.mVerticalSpeed = mVerticalSpeed;
    }
}
