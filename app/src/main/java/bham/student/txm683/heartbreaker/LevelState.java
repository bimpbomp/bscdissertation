package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private UniqueID uniqueID;

    private final Map map;

    private Player player;
    private ArrayList<Entity> nonPlayerEntities;

    private int screenWidth;
    private int screenHeight;

    public LevelState(Map map){
        this.uniqueID = new UniqueID();
        this.map = map;

        this.nonPlayerEntities = new ArrayList<>();

        freshInitFromMap();
    }

    public LevelState(String stateString) throws ParseException, JSONException {
        JSONObject jsonObject = new JSONObject(stateString);

        this.map = new Map(jsonObject.getString("mapname"));
        this.uniqueID = new UniqueID(jsonObject.getInt("uniqueidcounter"));

        this.player = new Player(jsonObject.getString("player"));

        JSONArray npes = jsonObject.getJSONArray("npes");

        this.nonPlayerEntities = new ArrayList<>();
        for (int i = 0; i < npes.length(); i++){
            this.nonPlayerEntities.add(new Entity((String)npes.get(i)));
        }
    }

    private void freshInitFromMap(){
        /*Point[] enemySpawns = this.map.getEnemySpawnLocations();
        for (Point enemySpawn : enemySpawns) {
            IsoscelesTriangle shape = new IsoscelesTriangle(enemySpawn, 50, 50, Color.BLUE);
            Entity entity = new Entity("NPE-" + uniqueID.id(), 150f, shape);
            this.nonPlayerEntities.add(entity);
        }*/

        this.nonPlayerEntities.add(new Entity("NPE-1", new Point(500,400), ShapeIdentifier.RECT, 500, 50, 150f, Color.BLUE));

        this.player = new Player("player", map.getPlayerSpawnLocation());
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

    public String getSaveString(){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("mapname", map.getName());
            jsonObject.put("uniqueidcounter", uniqueID.counter());

            jsonObject.put("player", player.getStateObject());

            JSONObject[] nPEStateString = new JSONObject[nonPlayerEntities.size()];
            for (int i = 0; i < nonPlayerEntities.size(); i++){
                nPEStateString[i] = nonPlayerEntities.get(i).getStateObject();
            }
            jsonObject.put("npes", new JSONArray(nPEStateString));

        } catch (JSONException e){
            //error parsing state for saving, abort
            return "";
        }

        return jsonObject.toString();
    }
}