package bham.student.txm683.heartbreaker;

import android.util.Log;

import static bham.student.txm683.heartbreaker.utils.Utils.sleep;

public class Level implements Runnable {
    private final String TAG = "hb::Level";
    private LevelState levelState;
    private boolean gameIsRunning;

    public Level(){
        this.levelState = new LevelState();
        this.gameIsRunning = true;
    }

    public void run(){
        Log.d(TAG, "run starting");


        while (gameIsRunning){

            this.gameIsRunning = false;
        }

        sleep(1000);

        Log.d(TAG, "run ending");
    }
}