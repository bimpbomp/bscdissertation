package bham.student.txm683.heartbreaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import bham.student.txm683.heartbreaker.intentbundleholders.LevelEnder;
import bham.student.txm683.heartbreaker.intentbundleholders.LevelLauncher;

public class MenuActivity extends Activity {
    public static final String BUNDLE_EXTRA = "bundle_extra";
    private LevelLauncher levelLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");

        TextView result = findViewById(R.id.levelResultView);
        if (bundle != null){
            LevelEnder levelEnder = new LevelEnder(bundle);

            String msg = "Result: " + levelEnder.isSuccess();
            result.setText(msg);
        } else {
            result.setVisibility(View.INVISIBLE);
        }

        this.levelLauncher = new LevelLauncher();



        String[] maps = new String[]{"Test map 1", "Test map 2", "Test map 3"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.text1, maps);

        //set the listview's adapter
        ListView listView = findViewById(R.id.map_list);
        listView.setAdapter(adapter);

        //add button listener
        Button button = findViewById(R.id.launch_button);
        button.setOnClickListener((v) -> {
            Intent exitIntent = new Intent(this, MainActivity.class);
            exitIntent.putExtra(BUNDLE_EXTRA, levelLauncher.createBundle());
            startActivity(exitIntent);
        });
    }
}
