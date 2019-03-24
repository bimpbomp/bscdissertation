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

    public Overlord(JSONObject jsonObject, int tileSize) throws JSONException {
        this.aliveEntities = new ArrayList<>();
        this.controlledDoors = new ArrayList<>();
        this.spawnPoints = new ArrayList<>();
        this.triggers = new ArrayList<>();

        JSONArray jsonArray = jsonObject.getJSONArray("lock_on_entry");
        for (int i = 0; i < jsonArray.length(); i++){
            controlledDoors.add(jsonArray.getString(i));
        }

        jsonArray = jsonObject.getJSONArray("spawn_points");
        for (int i = 0; i < jsonArray.length(); i++){
            spawnPoints.add(new Point(jsonArray.getJSONObject(i)).sMult(tileSize));
        }

        jsonArray = jsonObject.getJSONArray("triggers");
        for (int i = 0; i < jsonArray.length(); i++){
            triggers.add(new Point(jsonArray.getJSONObject(i)).sMult(tileSize));
        }

        maxAliveAtOnce = jsonObject.getInt("max_in_level");
        maxSpawns = jsonObject.getInt("spawns_in_wave");

        //TODO temp
        maxAliveAtOnce = 3;
        //maxSpawns = 100;

        inLockDown = false;

        uniqueID = new UniqueID();

        spawnerTimer = new GameTickTimer(1000 * 2);
        spawnerTimer.start();

        totalSpawns = 0;
    }

    private boolean spawnEntity(){
        BoundingBox visibleBounds = levelState.getLevelView().getVisibleBounds();

        for (Point spawn : spawnPoints){
            if (!visibleBounds.intersecting(spawn)){
                //spawn point not on screen
                AIEntity entity;

                Random r = new Random();

                int i = r.nextInt(100);

                boolean hasTurret = false;

                for (AIEntity alive : aliveEntities){
                    if (alive instanceof Turret){
                        hasTurret = true;
                        break;
                    }
                }

                if (!hasTurret)
                    entity = new Turret("T" + uniqueID.id(), spawn);
                else
                    entity = new Drone("D" + uniqueID.id(), spawn);

                addAI(entity);

                totalSpawns++;

                return true;
            }
        }
        return false;
    }

    public void update(float secondsSinceLastGameTick) {
        for (AIEntity aiEntity : aliveEntities) {
            aiEntity.tick(secondsSinceLastGameTick);
        }

        /*Log.d("OVERLORD","number of ai: " + aliveEntities.size());

        StringBuilder stringBuilder = new StringBuilder();
        for (AIEntity entity : aliveEntities){
            stringBuilder.append(entity.getName());
            stringBuilder.append(", ");
        }
        stringBuilder.append("END");
        Log.d("OVERLOAD", "controlling: " + stringBuilder.toString());*/

        if (!inLockDown && isTriggered()){
            lockDown(true);
        } else if (inLockDown && totalSpawns == maxSpawns && aliveEntities.size() == 0){
            lockDown(false);
        }

        if (inLockDown){
            if (spawnerTimer.tick() > 0 && aliveEntities.size() < maxAliveAtOnce && totalSpawns < maxSpawns){
                boolean spawned = spawnEntity();

                Log.d("OVERLORD", "spawned: " + spawned);
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
