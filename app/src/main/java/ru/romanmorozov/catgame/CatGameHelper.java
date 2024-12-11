package ru.romanmorozov.catgame;

import java.util.Random;

/**
 * Created by MorozovR on 6/9/2016.
 */

public class CatGameHelper {

    private Random r;

    public CatGameHelper() {
        r = new Random();
    }

    /**
     * Returns random integer withing the specified interval
     *
     * @param min - minimum value
     * @param max - maximum value
     * @return random integer withing the specified interval
     */

    public int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        return r.nextInt((max - min) + 1) + min;
    }

    public int getSpriteResource() {
        int i = getRandomNumberInRange(0, 10);
        switch (i) {
            case 0:
                return R.drawable.cat0;
            case 1:
                return R.drawable.cat1;
            case 2:
                return R.drawable.cat2;
            case 3:
                return R.drawable.cat3;
            case 4:
                return R.drawable.cat4;
            case 5:
                return R.drawable.cat5;
            case 6:
                return R.drawable.cat6;
            case 7:
                return R.drawable.cat7;
            case 8:
                return R.drawable.cat8;
            case 9:
                return R.drawable.cat9;
            case 10:
                return R.drawable.cat10;
            default:
                return R.drawable.cat0;
        }
    }
}
