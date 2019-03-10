package bham.student.txm683.heartbreaker;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import bham.student.txm683.heartbreaker.audio.AudioController;
import bham.student.txm683.heartbreaker.rendering.LevelView;

import java.io.*;

public class MainActivity extends Activity {

    private static final String TAG = "hb::MainActivity";

    private static final String SAVE_FILE_NAME = "bham.student.txm683.InLevelSaveFile";
    private static final String RESUME_FROM_SAVE_STATE_KEY = "resumeFromSavedState";

    private LevelView levelView;

    private LevelState levelState;
    private AudioController audioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        audioController = new AudioController(10);
        levelView = new LevelView(this);

        setContentView(levelView);

        if (savedInstanceState != null && savedInstanceState.getBoolean(RESUME_FROM_SAVE_STATE_KEY)){
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

        audioController.initSounds(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (levelState!= null)
            this.levelState.setReadyToRender(true);
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.levelState != null) {
            Log.d(TAG, "onResume levelstate not null");
            this.levelState.setPaused(true);
        }
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (levelState != null) {
            this.levelState = levelView.getLevelState();
            levelState.setPaused(true);
        }
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (levelState != null)
            this.levelState.setReadyToRender(false);

        /*String saveString = levelState.getSaveString();
        saveToFile(SAVE_FILE_NAME, saveString);*/

        audioController.releaseResources();

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
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
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

    public String readFromFile(String fileName){
        StringBuilder readInString = new StringBuilder();
        try {
            File file2 = new File(fileName);
            Log.d(TAG, "fileSize: " + file2.length());

            FileInputStream file = openFileInput(fileName);
            Log.d(TAG, "reading from " + fileName);

            InputStreamReader inputStreamReader = new InputStreamReader(file);

            char[] inputBuffer = new char[100];
            int charRead;

            while ((charRead = inputStreamReader.read(inputBuffer)) > 0){
                String s = String.copyValueOf(inputBuffer);
                readInString.append(s);
            }

            inputStreamReader.close();
        } catch (IOException e){
            return "";
        }
        return readInString.toString();
    }

    /*
    *    FileInputStream fileIn=openFileInput("mytextfile.txt");
         InputStreamReader InputRead= new InputStreamReader(fileIn);

         char[] inputBuffer= new char[READ_BLOCK_SIZE];
         String s="";
         int charRead;

         while ((charRead=InputRead.read(inputBuffer))>0) {
         // char to string conversion
         String readstring=String.copyValueOf(inputBuffer,0,charRead);
         s +=readstring;
         }
         InputRead.close();
    * */
}
