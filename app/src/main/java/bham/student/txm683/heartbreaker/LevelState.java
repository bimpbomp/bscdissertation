package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.map.Room;
import bham.student.txm683.heartbreaker.utils.DebugInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private Map map;

    private Player player;
    private ArrayList<AIEntity> enemyEntities;
    private CopyOnWriteArrayList<Projectile> bullets;

    private AIManager aiManager;

    private int screenWidth;
    private int screenHeight;

    private boolean readyToRender;
    private boolean paused;

    private DebugInfo debugInfo;

    private ReentrantLock bulletLock;

    public LevelState(Map map){
        this.map = map;

        this.enemyEntities = new ArrayList<>();
        this.bullets = new CopyOnWriteArrayList<>();

        this.readyToRender = false;
        this.paused = false;

        this.debugInfo = new DebugInfo();

        this.bulletLock = new ReentrantLock();
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

    public CopyOnWriteArrayList<Projectile> getBullets() {
        return bullets;
    }

    public void addBullet(Projectile[] bullets){
        this.bullets.addAll(Arrays.asList(bullets));
    }

    public void removeBullet(Projectile bullet){
        bullets.remove(bullet);
    }

    public Player getPlayer(){
        return map.getPlayer();
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

    public ArrayList<AIEntity> getEnemyEntities() {
        return map.getEnemies();
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