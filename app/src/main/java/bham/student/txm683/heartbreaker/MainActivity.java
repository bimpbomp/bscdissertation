package bham.student.txm683.heartbreaker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import bham.student.txm683.heartbreaker.rendering.LevelView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private static final String TAG = "hb::MainActivity";

    private static final String SAVE_FILE_NAME = "bham.student.txm683.InLevelSaveFile";
    private static final String RESUME_FROM_SAVE_STATE_KEY = "resumeFromSavedState";

    private LevelView levelView;

    private LevelState levelState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        levelView = new LevelView(this);

        setContentView(levelView);

        if (savedInstanceState.getBoolean(RESUME_FROM_SAVE_STATE_KEY)){
            Log.d(TAG, "savedInstanceState is not null");

            String stateString = readFromFile(SAVE_FILE_NAME);
            this.levelView.loadSaveFromStateString(stateString);
        } else {
            Log.d(TAG, "savedInstanceState is null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        levelView.setPaused(false);
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        levelState = levelView.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Entering onStop");
        super.onStop();
        String saveString = levelState.getSaveString();
        saveToFile(SAVE_FILE_NAME, saveString);
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(RESUME_FROM_SAVE_STATE_KEY, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void saveToFile(String fileName, String contents){
        FileOutputStream outputStream = null;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(contents.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e){
                //already closed
            }
        }
    }

    //TODO: implement file reading
    private String readFromFile(String fileName){
        String readInString = "";
        try {
            FileInputStream file = openFileInput(fileName);
            Log.d(TAG, "reading from " + fileName);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return readInString;
    }
}
