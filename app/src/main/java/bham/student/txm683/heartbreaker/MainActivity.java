package bham.student.txm683.heartbreaker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import bham.student.txm683.heartbreaker.rendering.LevelView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private static final String TAG = "hb::MainActivity";

    private static final String saveFileName = "bham.student.txm683.InLevelSaveFile";

    private LevelView levelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            Log.d(TAG, "savedInstanceState is not null");
        } else {
            Log.d(TAG, "savedInstanceState is null");
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        levelView = new LevelView(this);

        setContentView(levelView);
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
        levelView.setPaused(true);

        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void saveToFile(String fileName){
        FileOutputStream outputStream = null;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            String fileContents = "Helloworld!!!";
            outputStream.write(fileContents.getBytes());
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

    private void readFromFile(String fileName){
        try {
            FileInputStream file = openFileInput(fileName);
            Log.d(TAG, "reading from " + fileName);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
