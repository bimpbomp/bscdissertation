package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.ai.Chaser;
import bham.student.txm683.heartbreaker.ai.EnemyType;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.map.Room;
import bham.student.txm683.heartbreaker.utils.DebugInfo;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private UniqueID uniqueID;

    private Map map;

    private Player player;
    private HashMap<Tile, Entity> staticEntities;
    private ArrayList<AIEntity> enemyEntities;

    private AIManager aiManager;

    private int screenWidth;
    private int screenHeight;

    private boolean readyToRender;
    private boolean paused;

    private DebugInfo debugInfo;

    public LevelState(Map map){
        this.uniqueID = new UniqueID();
        this.map = map;

        this.staticEntities = new HashMap<>();
        this.enemyEntities = new ArrayList<>();

        this.readyToRender = false;
        this.paused = false;

        this.debugInfo = new DebugInfo();

        freshInitFromMap();
    }

    /*public LevelState(String stateString) throws ParseException, JSONException {
        JSONObject jsonObject = new JSONObject(stateString);

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
            this.enemyEntities.add(new Chaser(enemies.getString(i)));
        }

        this.debugInfo = new DebugInfo();
    }*/

    private void freshInitFromMap(){
        int playerSize = map.getTileSize();
        this.player = new Player("player", map.getPlayerSpawn(), playerSize, map.getTileSize()*3, Color.rgb(0,0,255));

        /*List<Point> staticSpawns = map.getStaticEntities();
        for (Point staticSpawn : staticSpawns){
            this.staticEntities.put(map.mapGlobalPointToTile(staticSpawn) , (new Boundary("B:"+uniqueID.id(), staticSpawn, map.getTileSize()+5, Color.rgb(32,32,32))));
        }*/

        for (Wall wall : map.getWalls()){
            wall.setName("W:"+uniqueID.id());

            StringBuilder stringBuilder = new StringBuilder();
            for (Point point : wall.getCollisionVertices()){
                stringBuilder.append(point.toString());
                stringBuilder.append(" - ");
            }
            stringBuilder.append("END");
            Log.d(TAG, wall.getName() + " collision: " + stringBuilder.toString());

            stringBuilder = new StringBuilder();
            for (Point point : wall.getRenderableVertices()){
                stringBuilder.append(point.toString());
                stringBuilder.append(" - ");
            }
            stringBuilder.append("END");
            Log.d(TAG, wall.getName() + " render: " + stringBuilder.toString());
        }

        List<Pair<EnemyType, Point>> enemySpawns = map.getEnemies();
        Point enemySpawn;
        EnemyType enemyType;
        for (Pair pair : enemySpawns){
            enemySpawn = (Point)pair.second;
            enemyType = (EnemyType)pair.first;

            ShapeIdentifier shapeIdentifier;

            switch (enemyType){
                case CHASER:
                    shapeIdentifier = ShapeIdentifier.KITE;
                    break;
                default:
                    shapeIdentifier = ShapeIdentifier.RECT;
                    break;
            }

            this.enemyEntities.add(new Chaser("C:" + uniqueID.id(), enemySpawn, map.getTileSize()/2, Color.rgb(255, 153, 51), 300f, this));
        }
    }

    public void addStaticEntity(Entity entity){
        this.staticEntities.put(map.mapGlobalPointToTile(entity.getSpawnCoordinates()), entity);
    }

    public boolean hasEntityAtTile(Tile tile){
        return staticEntities.containsKey(tile);
    }

    public HashMap<Tile, Entity> getStaticEntities() {
        return staticEntities;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void removeEnemy(MoveableEntity entity){
        for (MoveableEntity enemy : enemyEntities){
            if (enemy.getName().equals(entity.getName())){
                enemyEntities.remove(enemy);
                break;
            }
        }
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

        /*try {
            jsonObject.put("mapname", map.getName());
            jsonObject.put("uniqueidcounter", uniqueID.counter());

            jsonObject.put("player", player.getStateObject());

            JSONObject[] staticsStateString = convertEntityListToJSONObjectArray(staticEntities.toArray(new Entity[0]));
            jsonObject.put("statics", new JSONArray(staticsStateString));

            JSONObject[] enemiesStateString = convertEntityListToJSONObjectArray(enemyEntities.toArray(new Entity[0]));
            jsonObject.put("enemies", new JSONArray(enemiesStateString));

        } catch (JSONException e){
            //error parsing state for saving, abort
            return "";
        }*/

        return jsonObject.toString();
    }

    private JSONObject[] convertEntityListToJSONObjectArray(Entity[] entityList) throws JSONException{
        JSONObject[] jsonObjects = new JSONObject[entityList.length];
        for (int i = 0; i < entityList.length; i++){
            jsonObjects[i] = entityList[i].getStateObject();
        }
        return jsonObjects;
    }

    public ArrayList<AIEntity> getEnemyEntities() {
        return enemyEntities;
    }

    public boolean isReadyToRender() {
        return readyToRender;
    }

    public void setReadyToRender(boolean readyToRender) {
        this.readyToRender = readyToRender;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    public AIManager getAiManager() {
        return aiManager;
    }

    public void setAiManager(AIManager aiManager) {
        this.aiManager = aiManager;
    }

    public boolean inSameRoom(Entity entity1, Entity entity2){
        for (Room room : map.getRooms().values()){
            if (room.isEntityInRoom(entity1)){
                return room.isEntityInRoom(entity2);
            }
        }
        return false;
    }
}