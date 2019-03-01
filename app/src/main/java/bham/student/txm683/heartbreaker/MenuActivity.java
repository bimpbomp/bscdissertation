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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends Activity {
    public static final String BUNDLE_EXTRA = "bundle_extra";
    private LevelLauncher levelLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");

        //if the activity was created by the player leaving a level, then display the result of that level
        TextView result = findViewById(R.id.levelResultView);
        if (bundle != null){
            LevelEnder levelEnder = new LevelEnder(bundle);

            String msg;

            switch (levelEnder.getStatus()){

                case USER_QUIT:
                    msg = "User Quit";
                    break;
                case PLAYER_DIED:
                    msg = "Level Failed...";
                    break;
                case CORE_DESTROYED:
                    msg = "Level Complete!";
                    break;
                case ERROR:
                    msg = "Error Occurred";
                    break;
                default:
                    msg = "Something went wrong :o";
                    break;
            }

            result.setText(msg);
        } else {
            result.setVisibility(View.INVISIBLE);
        }

        //bundle builder for holding parameters needed for level launch
        this.levelLauncher = new LevelLauncher();


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.text1, listRaw());

        //the below is the R.id value of the given field (gotten from listRaw())
        //int resourceID=fields[count].getInt(fields[count]);

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

    public List<String> listRaw(){
        List<String> list = new ArrayList<>();

        Field[] fields=R.raw.class.getFields();

        for(Field field : fields){
            list.add(field.getName());
        }
        return list;
    }
}
