package bham.student.txm683.heartbreaker;

import android.content.res.Resources;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.utils.Vector;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private Player player;

    private int screenWidth;
    private int screenHeight;

    public LevelState(){
        this.player = new Player("entity", new Vector(100,100));
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public Player getPlayer(){
        return this.player;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}