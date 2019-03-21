package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Portal;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.intentbundleholders.LevelEnder;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.utils.DebugInfo;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class LevelState {
    private static final String TAG = "hb::LevelState";

    private Map map;

    private Player player;

    private List<AIEntity> aliveAIEntities;
    private List<AIEntity> deadAIEntities;

    private CopyOnWriteArrayList<Projectile> bullets;
    private CopyOnWriteArrayList<Explosion> explosions;

    private Tile screenDimensions;

    private Core core;

    private Portal portal;

    private CopyOnWriteArrayList<Pickup> pickups;

    private CopyOnWriteArrayList<Explosion> lingeringExplosions;

    private AIManager aiManager;

    private boolean readyToRender;
    private boolean paused;

    private DebugInfo debugInfo;

    private LevelEnder levelEnder;

    private CollisionManager collisionManager;

    private LevelView levelView;

    private Set<Integer> blockedMeshPolygons;

    public LevelState(Map map){
        this.map = map;

        this.player = map.getPlayer();

        this.aliveAIEntities = map.getEnemies();
        this.deadAIEntities = new ArrayList<>();

        this.bullets = new CopyOnWriteArrayList<>();

        this.explosions = new CopyOnWriteArrayList<>();
        this.lingeringExplosions = new CopyOnWriteArrayList<>();

        this.readyToRender = false;
        this.paused = false;

        this.debugInfo = new DebugInfo();

        this.pickups = new CopyOnWriteArrayList<>(map.getPickups());

        this.portal = map.getPortal();

        this.core = null;
        for (AIEntity aiEntity : aliveAIEntities){
            if (aiEntity instanceof Core){
                this.core = (Core) aiEntity;
            }
        }

        levelEnder = new LevelEnder();

        this.blockedMeshPolygons = new HashSet<>();
    }

    public LevelView getLevelView() {
        return levelView;
    }

    public void setLevelView(LevelView levelView) {
        this.levelView = levelView;
    }

    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public Portal getPortal() {
        return portal;
    }

    public Tile getScreenDimensions() {
        return screenDimensions;
    }

    public void setScreenDimensions(int width, int height) {
        this.screenDimensions = new Tile(width, height);
    }

    public int mapToMesh(Point p){

        for (MeshPolygon meshPolygon : getRootMeshPolygons().values()){
            if (meshPolygon.getBoundingBox().intersecting(p)){
                return meshPolygon.getId();
            }
        }
        return -1;
    }

    public void clearBlockedPolygons(){
        blockedMeshPolygons.clear();
    }

    public void addBlockedPolygon(int id){
        blockedMeshPolygons.add(id);
    }

    public Set<Integer> getBlockedPolygons() {
        return blockedMeshPolygons;
    }

    public Graph<Integer> getMeshGraph(){
        return map.getMeshGraph();
    }

    public HashMap<Integer, MeshPolygon> getRootMeshPolygons(){
        return map.getRootMeshPolygons();
    }

    public LevelEnder getLevelEnder(){
        return levelEnder;
    }

    public List<AIEntity> getDeadAI(){
        return deadAIEntities;
    }

    public void aiDied(AIEntity aiEntity){
        Log.d(TAG, aiEntity.getName() + " died");

        aiEntity.setColor(Color.rgb(20, 20, 20));
        aliveAIEntities.remove(aiEntity);
        deadAIEntities.add(aiEntity);
        aiManager.removeAI(aiEntity);

        aiEntity.onDeath();
    }

    public List<Collidable> getAvoidables(){
        List<Collidable> collidables = new ArrayList<>(aliveAIEntities);
        collidables.add(map.getPlayer());
        collidables.addAll(explosions);
        collidables.addAll(bullets);

        return collidables;
    }

    public List<Collidable> getNonStaticCollidables(){
        List<Collidable> collidables = new ArrayList<>(aliveAIEntities);
        collidables.add(map.getPlayer());
        collidables.addAll(explosions);
        collidables.addAll(bullets);
        collidables.addAll(pickups);

        if (portal != null)
            collidables.add(portal);

        return collidables;
    }

    public List<Collidable> getStaticCollidables(){

        List<Collidable> collidables = new ArrayList<>(map.getDoors().values());
        collidables.addAll(map.getWalls());


        return collidables;
    }

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
        return player;
    }

    public Map getMap() {
        return map;
    }

    public List<AIEntity> getAliveAIEntities() {
        return this.aliveAIEntities;
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
}