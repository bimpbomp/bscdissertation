package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.Entity;

import java.util.HashMap;

public class BContext {
    private HashMap<String, Object> pairs;

    public static final String MOVE_TO_KEY = "move_target";
    public static final String CONTROLLED_ENTITY_KEY = "controlled_entity";
    public static final String FLEE_FROM_KEY = "attack_target";
    public static final String HEALTH_BOUND_KEY = "health_bound";
    public static final String ATTACK_TARGET_KEY = "attack_target";
    public static final String LEVEL_STATE_KEY = "level_state";

    public BContext(){
        pairs = new HashMap<>();
    }

    public void addPair(String key, Object value){
        pairs.put(key, value);
    }

    public Object getValue(String key){
        return pairs.getOrDefault(key, null);
    }

    public boolean containsKey(String key){
        return pairs.containsKey(key);
    }

    public boolean containsMoveTo(){
        return pairs.containsKey(MOVE_TO_KEY) && pairs.get(MOVE_TO_KEY) instanceof Entity;
    }

    public boolean containsControlledEntity(){
        return pairs.containsKey(CONTROLLED_ENTITY_KEY) && pairs.get(CONTROLLED_ENTITY_KEY) instanceof AIEntity;
    }

    public boolean containsFleeFrom(){
        return pairs.containsKey(FLEE_FROM_KEY) && pairs.get(FLEE_FROM_KEY) instanceof Entity;
    }

    public boolean containsHealthBound(){
        return pairs.containsKey(HEALTH_BOUND_KEY) && pairs.get(HEALTH_BOUND_KEY) instanceof Integer;
    }

    public boolean containsAttackTarget(){
        return pairs.containsKey(ATTACK_TARGET_KEY) && pairs.get(ATTACK_TARGET_KEY) instanceof AIEntity;
    }

    public boolean containsLevelState(){
        return pairs.containsKey(LEVEL_STATE_KEY) && pairs.get(LEVEL_STATE_KEY) instanceof LevelState;
    }
}
