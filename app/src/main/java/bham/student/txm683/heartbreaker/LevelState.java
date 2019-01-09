package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.map.Map;

import java.util.ArrayList;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private Player player;
    private Map map;

    private ArrayList<Entity> nonPlayerEntities;

    private int screenWidth;
    private int screenHeight;

    public LevelState(){
        this.map = new Map();

        this.nonPlayerEntities = new ArrayList<>();
    }

    public LevelState(String saveString){

    }

    public void addNonPlayerEntity(Entity entity){
        this.nonPlayerEntities.add(entity);
    }

    public ArrayList<Entity> getNonPlayerEntities(){
        return this.nonPlayerEntities;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public Player getPlayer(){
        return this.player;
    }

    public Map getMap() {
        return map;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    //TODO implement save state feature
    public String getSaveString(){

        String saveString = "";

        return saveString;
    }
}