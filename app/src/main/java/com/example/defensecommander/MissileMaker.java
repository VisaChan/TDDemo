package com.example.defensecommander;

import static com.example.defensecommander.Interceptor.INTERCEPTOR_BLAST;

import android.animation.AnimatorSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class MissileMaker implements Runnable {
    private static final String TAG = "PlaneMaker";
    private final MainActivity mainActivity;
    private boolean isRunning;
    private final ArrayList<Missile> activeMissiles = new ArrayList<>();
    private final int screenWidth;
    private final int screenHeight;
    private static final int NUM_LEVELS = 20;
    private int MISSILES_PER_LEVEL = 10;
    private static final int SLEEP_BETWEEN_LEVELS = 2000;
    private long delay = 20 * 250;

    public static ArrayList<ImageView> bases = new ArrayList<>();

    MissileMaker(MainActivity mainActivity, int screenWidth, int screenHeight) {
        this.mainActivity = mainActivity;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        ImageView base1 = mainActivity.findViewById(R.id.imageView);
        ImageView base2 = mainActivity.findViewById(R.id.imageView2);
        ImageView base3 = mainActivity.findViewById(R.id.imageView3);
        bases.add(base1);
        bases.add(base2);
        bases.add(base3);
    }

    void setRunning(boolean running) {
        isRunning = running;
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);
        for (Missile m : temp) {
            m.stop();
        }
    }

    void freezeMissiles(){
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);
        for (Missile m : temp){
            m.pause();
        }
    }

    void resumeMissiles(){
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);
        for (Missile m : temp){
            m.resume();
        }
    }

    @Override
    public void run() {
        //if else sleep did not work


        setRunning(true);
        int missileCount = 0;
        while (isRunning) {

            int resId = R.drawable.missile;
            long missileTime = (long) ((delay * 0.5) + (Math.random() * delay));

            int extraDelay = 0;

            final Missile missile = new Missile(screenWidth, screenHeight, extraDelay + missileTime, mainActivity);

            activeMissiles.add(missile);
            final AnimatorSet as = missile.setData(resId);
            missileCount++;

            mainActivity.runOnUiThread(as::start);


            try {
                Thread.sleep((long) (getSleepTime()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(missileCount > MISSILES_PER_LEVEL){
                missileCount = 0;
                delay -= 500;
                if (delay <= 0)
                    delay = 1;
                Log.d(TAG, "run: DELAY: " + delay);
                mainActivity.increaseLevel();
                try {
                    Thread.sleep((long) (SLEEP_BETWEEN_LEVELS));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        /*setRunning(true);
        int missileCount = 0;
        while (isRunning) {

            if(GameState.checkFreeze() == true){
                try {
                    Thread.sleep((long) (1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                int resId = R.drawable.missile;
                long missileTime = (long) ((delay * 0.5) + (Math.random() * delay));
                final Missile missile = new Missile(screenWidth, screenHeight, missileTime, mainActivity);
                activeMissiles.add(missile);
                final AnimatorSet as = missile.setData(resId);
                missileCount++;


                mainActivity.runOnUiThread(as::start);

                try {
                    Thread.sleep((long) (getSleepTime()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(missileCount > MISSILES_PER_LEVEL){
                    missileCount = 0;
                    delay -= 500;
                    if (delay <= 0)
                        delay = 1;
                    Log.d(TAG, "run: DELAY: " + delay);
                    mainActivity.increaseLevel();
                    try {
                        Thread.sleep((long) (SLEEP_BETWEEN_LEVELS));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }

        }*/

    }

    private double getSleepTime(){
        double rand = Math.random();
        if(rand<0.1) return 1;
        if(rand<0.2 )return 0.5*delay;
        return delay;
    }


    void removeMissile(Missile m) {
        activeMissiles.remove(m);
    }


    public void applyMissileBlast(Missile missile, int id) {

        Log.d(TAG, "applyMissileBlast: -------------------------- " + id);

        float x1 = missile.getX();
        float y1 = missile.getY();

        Log.d(TAG, "applyMissileBlast: MISSILE: " + x1 + ", " + y1);

        ImageView baseToRemove = null;
        for (ImageView iv : bases) {

            //Get size of the base image
            float x2 = (int) (iv.getX() + (0.5 * iv.getWidth()));
            float y2 = (int) (iv.getY() + (0.5 * iv.getHeight()));

            Log.d(TAG, "applyMissileBlast:    Base: " + x2 + ", " + y2);

            float f = (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            Log.d(TAG, "applyMissileBlast:    DIST: " + f);

            //If distance between missile and base is <250
            if (f < 250) {
                SoundPlayer.getInstance().start("base_blast");
                Log.d(TAG, "applyMissileBlast:    Hit: " + f);
                missile.missileBlast(x2, y2); //blast destroy base
                baseToRemove = iv;
                mainActivity.getLayout().removeView(iv);
                if(GameState.checkFreeze() == false){ //WARNING
                  //  GameState.changeFreeze(); //WARNING
                    freezeMissiles();
                } //WARNING
            }

            Log.d(TAG, "applyMissileBlast: --------------------------");
        }
        if (baseToRemove == null) {
            SoundPlayer.getInstance().start("missile_miss");
        } else {
            bases.remove(baseToRemove);
            if(bases.size()<1){
                mainActivity.gameOver();
            }
        }
    }

    public void applyInterceptorBlast(Interceptor interceptor, int id) {
        Log.d(TAG, "applyInterceptorBlast: -------------------------- " + id);

        float x1 = interceptor.getX();
        float y1 = interceptor.getY();

        Log.d(TAG, "applyInterceptorBlast: INTERCEPTOR: " + x1 + ", " + y1);

        ArrayList<Missile> nowGone = new ArrayList<>();
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);

        for (Missile m : temp) {

            float x2 = (int) (m.getX() + (0.5 * m.getWidth()));
            float y2 = (int) (m.getY() + (0.5 * m.getHeight()));

            Log.d(TAG, "applyInterceptorBlast:    Missile: " + x2 + ", " + y2);


            float f = (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            Log.d(TAG, "applyInterceptorBlast:    DIST: " + f);

            if (f < 120) {
                SoundPlayer.getInstance().start("interceptor_hit_missile");
                mainActivity.incrementScore();
                Log.d(TAG, "applyInterceptorBlast:    Hit: " + f);
                m.interceptorBlast(x2, y2);
                nowGone.add(m);
            }

            Log.d(TAG, "applyInterceptorBlast: --------------------------");

        }

        for (Missile m : nowGone) {
            activeMissiles.remove(m);
        }
    }
}
