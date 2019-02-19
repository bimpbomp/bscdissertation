package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.map.Room;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.DebugInfo;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private Map map;

    private Player player;
    private ArrayList<AIEntity> enemyEntities;
    private CopyOnWriteArrayList<Projectile> bullets;
    private CopyOnWriteArrayList<Explosion> explosions;

    private Core core;

    private CopyOnWriteArrayList<Pickup> pickups;

    private CopyOnWriteArrayList<Explosion> lingeringExplosions;

    private AIManager aiManager;

    private int screenWidth;
    private int screenHeight;

    private boolean readyToRender;
    private boolean paused;

    private DebugInfo debugInfo;

    private Graph<Tile> graph;

    public LevelState(Map map){
        this.map = map;

        this.enemyEntities = new ArrayList<>();
        this.bullets = new CopyOnWriteArrayList<>();

        this.explosions = new CopyOnWriteArrayList<>();
        this.lingeringExplosions = new CopyOnWriteArrayList<>();

        this.readyToRender = false;
        this.paused = false;

        this.debugInfo = new DebugInfo();

        this.pickups = new CopyOnWriteArrayList<>(map.getPickups());

        this.core = map.getCore();

        for (AIEntity entity : enemyEntities){
            entity.setLevelState(this);
        }

        generateGraph();

    }

    public List<Collidable> getNonStaticCollidables(){
        List<Collidable> collidables = new ArrayList<>(map.getEnemies());
        collidables.add(map.getPlayer());
        collidables.addAll(explosions);
        collidables.addAll(bullets);
        collidables.addAll(pickups);
        collidables.add(core);

        return collidables;
    }

    public List<Collidable> getStaticCollidables(){
        //TODO consolidate into one list in LevelState
        //TODO store as HashMap with id/name as key and the object as the value

        List<Collidable> collidables = new ArrayList<>(map.getDoors().values());
        collidables.addAll(map.getWalls());


        return collidables;
    }

    public List<Renderable> getRenderables(){
        //TODO add getRenderPriority() method to LevelState
        //this will allow renderables to be ordered by their render priority and allow higher priority items to
        //be rendered on the top (i.e. last)
        return new ArrayList<>();
    }

    private void generateGraph(){
        this.graph = new Graph<>();

        Tile[] nodeTiles = new Tile[]{
                //room 0
                new Tile(500,500),

                //room 1
                new Tile(1200,1200),

                //room 2
                new Tile(1900, 500),

                //room 3
                new Tile(600,2000),

                //door 0
                new Tile(700,700),
                new Tile(900,700),
                new Tile(1100,700),

                //door 1
                new Tile(1300,700),
                new Tile(1500,700),
                new Tile(1700,700),

                //door 2
                new Tile(700,1700),
                new Tile(900,1700),
                new Tile(1100,1700)
        };

        for (Tile tile : nodeTiles){
            graph.addNode(tile);
        }

        addConnection(nodeTiles, 0, 4);
        addConnection(nodeTiles, 4, 5);
        addConnection(nodeTiles, 5, 6);
        addConnection(nodeTiles, 6, 7);
        addConnection(nodeTiles, 6, 1);
        addConnection(nodeTiles, 6, 12);
        addConnection(nodeTiles, 7, 1);
        addConnection(nodeTiles, 7, 8);
        addConnection(nodeTiles, 8, 9);
        addConnection(nodeTiles, 9, 2);
        addConnection(nodeTiles, 1, 12);
        addConnection(nodeTiles, 12, 11);
        addConnection(nodeTiles, 11, 10);
        addConnection(nodeTiles, 10, 3);
    }

    private void addConnection(Tile[] nodeTiles, int i, int j){
        graph.addConnection(graph.getNode(nodeTiles[i]), graph.getNode(nodeTiles[j]),
                AIEntity.calculateEuclideanHeuristic(nodeTiles[i], nodeTiles[j]));
    }

    public Graph<Tile> getGraph() {
        return graph;
    }

    public TileSet getTileSet(){
        return map.getTileSet();
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
            this.enemyEntities.add(new Drone(enemies.getString(i)));
        }

        this.debugInfo = new DebugInfo();
    }*/

    public Core getCore() {
        return core;
    }

    public CopyOnWriteArrayList<Pickup> getPickups() {
        return pickups;
    }

    public void removePickup(Pickup pickup){
        pickups.remove(pickup);
    }

    public void setPickups(Collection<Pickup> collection){
        pickups.addAll(collection);
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public void setScreenDimensions(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void removeEnemy(AIEntity entity){
        enemyEntities.remove(entity);
    }

    public CopyOnWriteArrayList<Explosion> getExplosions() {
        return explosions;
    }

    public void addExplosion(Explosion explosion){
        this.explosions.add(explosion);
        this.lingeringExplosions.add(explosion);
    }

    public CopyOnWriteArrayList<Explosion> getLingeringExplosions(){
        return lingeringExplosions;
    }

    public void removeLingeringExplosions(){
        this.lingeringExplosions.clear();
    }

    public void removeExplosions(){
        this.explosions.clear();
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