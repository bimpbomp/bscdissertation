package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
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
    private ArrayList<Entity> staticEntities;
    private ArrayList<MoveableEntity> enemyEntities;

    private int screenWidth;
    private int screenHeight;

    public LevelState(Map map){
        this.uniqueID = new UniqueID();
        this.map = map;

        this.staticEntities = new ArrayList<>();
        this.enemyEntities = new ArrayList<>();

        freshInitFromMap();
    }

    public LevelState(String stateString) throws ParseException, JSONException {
        JSONObject jsonObject = new JSONObject(stateString);

        this.map = new Map();
        this.map.loadMap("TestMap", 50);
        this.uniqueID = new UniqueID(jsonObject.getInt("uniqueidcounter"));

        this.player = new Player(jsonObject.getString("player"));

        JSONArray statics = jsonObject.getJSONArray("statics");

        this.staticEntities = new ArrayList<>();
        for (int i = 0; i < statics.length(); i++){
            this.staticEntities.add(new Entity((String)statics.get(i)));
        }

        JSONArray enemies = jsonObject.getJSONArray("enemies");

        this.enemyEntities = new ArrayList<>();
        for (int i = 0; i < enemies.length(); i++){
            this.enemyEntities.add(new MoveableEntity(enemies.getString(i)));
        }
    }

    private void freshInitFromMap(){
        /*this.staticEntities.add(new Entity("NPE-1", new Point(500,400), ShapeIdentifier.RECT, 500, 50, Color.BLUE));

        this.player = new Player("player", map.getPlayerSpawnLocation());*/

        Log.d(TAG, "player spawn: " + map.getPlayerSpawnLocation());
        this.player = new Player("player", map.getPlayerSpawnLocation(), map.getTileSize()/2, map.getTileSize() * 2);

        Point[] staticSpawns = map.getStaticSpawns();
        for (Point staticSpawn : staticSpawns){
            Log.d(TAG, "staticSpawn: "+ staticSpawn.toString());
            this.staticEntities.add(new Entity("Static-"+uniqueID.id(), staticSpawn, ShapeIdentifier.RECT, map.getTileSize(), map.getTileSize(), Color.BLUE));
        }

        Pair[] enemySpawns = map.getEnemySpawnLocations();
        for (Pair pair : enemySpawns){
            Point enemySpawn = (Point)pair.second;
            Log.d(TAG, "enemySpawn: " + enemySpawn.toString());

            ShapeIdentifier shapeIdentifier;

            if ((int) pair.first == 3){
                shapeIdentifier = ShapeIdentifier.RECT;
            } else if ((int) pair.first == 4){
                shapeIdentifier = ShapeIdentifier.ISO_TRIANGLE;
            } else {
                shapeIdentifier = ShapeIdentifier.INVALID;
            }

            this.enemyEntities.add(new MoveableEntity("E-" + uniqueID.id(), enemySpawn, shapeIdentifier, map.getTileSize()/2, map.getTileSize()/2, Color.YELLOW, 200f));
        }
    }

    public void addStaticEntity(Entity entity){
        this.staticEntities.add(entity);
    }

    public ArrayList<Entity> getStaticEntities(){
        return this.staticEntities;
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

            JSONObject[] staticsStateString = new JSONObject[staticEntities.size()];
            for (int i = 0; i < staticEntities.size(); i++){
                staticsStateString[i] = staticEntities.get(i).getStateObject();
            }
            jsonObject.put("statics", new JSONArray(staticsStateString));

            JSONObject[] enemiesStateString = new JSONObject[enemyEntities.size()];
            for (int i = 0; i < enemyEntities.size(); i++){
                enemiesStateString[i] = enemyEntities.get(i).getStateObject();
            }
            jsonObject.put("enemies", new JSONArray(enemiesStateString));

        } catch (JSONException e){
            //error parsing state for saving, abort
            return "";
        }

        return jsonObject.toString();
    }

    public ArrayList<MoveableEntity> getEnemyEntities() {
        return enemyEntities;
    }

    /*private JSONObject[] getEntityArrayListStateString(ArrayList<Entity> arrayList) throws JSONException{
        JSONObject[] listStateString = new JSONObject[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++){
            listStateString[i] = arrayList.get(i).getStateObject();
        }
        return listStateString;
    }*/
}