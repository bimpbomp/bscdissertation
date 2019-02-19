package bham.student.txm683.heartbreaker.ai.behaviours;

import java.util.HashMap;

public class BContext {
    private HashMap<String, Object> pairs;

    public static final String MOVE_TO = "move_target";
    public static final String HOST_ENTITY = "controlled_entity";
    public static final String FLEE_FROM = "attack_target";
    public static final String HEALTH_BOUND = "health_bound";
    public static final String ATTACK_TARGET = "attack_target";
    public static final String LEVEL_STATE = "level_state";
    public static final String VIEW_RANGE = "view_range";
    public static final String SIGHT_BLOCKED = "sight_blocked";

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
}
