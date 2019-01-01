package bham.student.txm683.heartbreaker;

import android.content.res.Resources;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Vector;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private Entity entity;

    private int screenWidth;
    private int screenHeight;

    public LevelState(){
        this.entity = new Entity("entity", new Vector(100,100));
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public Entity getEntity(){
        return this.entity;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}