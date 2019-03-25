package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.GameTickTimer;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Overlord {
    private List<AIEntity> aliveEntities;

    private List<String> controlledDoors;
    private List<Point> spawnPoints;
    private int maxAliveAtOnce;
    private int maxSpawns;

    private List<Point> triggers;

    private LevelState levelState;

    private boolean inLockDown;

    private UniqueID uniqueID;

    private GameTickTimer spawnerTimer;

    private int totalSpawns;

    private Random r;

    public Overlord(JSONObject jsonObject, int tileSize) throws JSONException {
        this.aliveEntities = new ArrayList<>();
        this.controlledDoors = new ArrayList<>();
        this.spawnPoints = new ArrayList<>();
        this.triggers = new ArrayList<>();

        r = new Random();

        JSONArray jsonArray;

        if (jsonObject.has("lock_on_entry")) {
            jsonArray = jsonObject.getJSONArray("lock_on_entry");
            for (int i = 0; i < jsonArray.length(); i++) {
                controlledDoors.add(jsonArray.getString(i));
            }
        }

        if (jsonObject.has("spawn_points")) {
            jsonArray = jsonObject.getJSONArray("spawn_points");
            for (int i = 0; i < jsonArray.length(); i++) {
                spawnPoints.add(new Point(jsonArray.getJSONObject(i)).sMult(tileSize));
            }
        }

        if (jsonObject.has("triggers")) {
            jsonArray = jsonObject.getJSONArray("triggers");
            for (int i = 0; i < jsonArray.length(); i++) {
                triggers.add(new Point(jsonArray.getJSONObject(i)).sMult(tileSize));
            }

            inLockDown = false;
        } else {
            inLockDown = true;
        }

        if (jsonObject.has("max_in_level"))
            maxAliveAtOnce = jsonObject.getInt("max_in_level");
        else
            maxAliveAtOnce = 2;

        if (jsonObject.has("spawns_in_wave"))
            maxSpawns = jsonObject.getInt("spawns_in_wave");
        else
            maxSpawns = 4;

        maxSpawns = 1;

        uniqueID = new UniqueID();

        spawnerTimer = new GameTickTimer(1000 * 5);
        spawnerTimer.start();

        totalSpawns = 0;
    }

    public boolean isDefeated(){
        return aliveEntities.size() == 0 && maxSpawns == totalSpawns;
    }

    private void spawnEntity(){
        BoundingBox visibleBounds = levelState.getLevelView().getVisibleBounds();

        Point spawn = null;
        for (Point potentialSpawn : spawnPoints){
            if (!visibleBounds.intersecting(potentialSpawn)){
                //spawn point not on screen
                spawn = potentialSpawn;
            }
        }

        int randomNumber;
        if (spawn == null) {
            //no spawn points off screen
            randomNumber = r.nextInt(spawnPoints.size());

            spawn = spawnPoints.get(randomNumber);
        }

        randomNumber = r.nextInt(150);
        AIEntity entity;
        if (randomNumber < 40)
            entity = new Turret("T" + uniqueID.id(), spawn);
        else {
            randomNumber = r.nextInt(100);

            if (randomNumber < 60)
                entity = new Drone("D" + uniqueID.id(), spawn, false);
            else
                entity = new Drone("D" + uniqueID.id(), spawn, true);
        }

        addAI(entity);

        totalSpawns++;
    }

    public void update(float secondsSinceLastGameTick) {

        if (!inLockDown && isTriggered()){
            lockDown(true);
        } else if (inLockDown && totalSpawns == maxSpawns && aliveEntities.size() == 0){
            lockDown(false);
        }

        if (inLockDown){
            //if the player is in the arena

            //spawn an enemy if you can, otherwise reset the timer
            if (spawnerTimer.tick() > 0 && aliveEntities.size() < maxAliveAtOnce && totalSpawns < maxSpawns){
                spawnEntity();
            }

            //tick all alive enemies.
            List<AIEntity> dead = new ArrayList<>();
            for (AIEntity aiEntity : aliveEntities) {
                aiEntity.tick(secondsSinceLastGameTick);

                if (aiEntity.getHealth() < 1)
                    dead.add(aiEntity);
            }

            for (AIEntity entity: dead){
                levelState.aiDied(entity);
            }
        }
    }

    private boolean isTriggered(){

        for (Point trigger : triggers){
            int id = levelState.mapToMesh(trigger);

            Log.d("OVERLORD", "trigger: " + trigger + ", id: " + id + ", player mesh: " + levelState.getPlayer().getMesh());
            if (levelState.getPlayer().getMesh() == id){
                return true;
            }
        }
        return false;
    }

    private void lockDown(boolean lock){

        for (String doorName : controlledDoors){
            if (levelState.getMap().getDoors().containsKey(doorName))
                levelState.getMap().getDoors().get(doorName).setLocked(lock);
        }

        inLockDown = lock;
    }

    public LevelState getLevelState() {
        return levelState;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }

    public void addAI(AIEntity ai){
        if (!aliveEntities.contains(ai)) {
            this.aliveEntities.add(ai);
            levelState.getAliveAIEntities().add(ai);
            ai.setLevelState(levelState);
            ai.setOverlord(this);
        }
    }

    public void removeAI(AIEntity ai){
        aliveEntities.remove(ai);
    }

    public List<AIEntity> getAliveEntities() {
        return aliveEntities;
    }
}
