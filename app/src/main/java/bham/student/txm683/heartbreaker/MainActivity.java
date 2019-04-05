package bham.student.txm683.heartbreaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import bham.student.txm683.heartbreaker.intentbundleholders.LevelLauncher;
import bham.student.txm683.heartbreaker.rendering.LevelView;

import java.io.*;

public class MainActivity extends Activity {

    private static final String TAG = "hb::MainActivity";

    private static final String RESUME_FROM_SAVE_STATE_KEY = "resumeFromSavedState";

    private LevelView levelView;

    private LevelState levelState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(MenuActivity.BUNDLE_EXTRA);

        String mapName = "";
        if (bundle != null){
            LevelLauncher levelLauncher = new LevelLauncher(bundle);
            mapName = levelLauncher.getMapName();
        }
        levelView = new LevelView(this, mapName);

        setContentView(levelView);
    }

    @Override
    public void onBackPressed() {
        levelView.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    public void appendToFile(String contents){
        FileOutputStream outputStream = null;
        OutputStreamWriter outStreamWriter = null;

        try {

            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            File myDir = new File(root + "/benchlogs");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            Log.d("root", root);

            File file = new File(myDir, "log-"+System.currentTimeMillis());

            Log.d("root", "file exists: "+  file.exists());

            if (!file.exists()){
                file.createNewFile();
            }

            outputStream = new FileOutputStream(file, true);
            outStreamWriter = new OutputStreamWriter(outputStream);

            outStreamWriter.append(contents);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();

                if (outStreamWriter != null)
                    outStreamWriter.close();

            } catch (Exception e){
                //already closed
            }
        }
    }

    public String readFromFile(InputStream inputStream){
        StringBuilder readInString = new StringBuilder();
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

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
}
