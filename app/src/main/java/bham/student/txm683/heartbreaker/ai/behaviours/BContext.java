package bham.student.txm683.heartbreaker.ai.behaviours;

import java.util.HashMap;

public class BContext {
    private HashMap<String, Object> pairs;

    public BContext(){
        pairs = new HashMap<>();
    }

    public void addPair(String key, Object value){
        pairs.put(key, value);
    }

    public Object getValue(String key){
        return pairs.getOrDefault(key, null);
    }
}
