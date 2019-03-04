package bham.student.txm683.heartbreaker.ai.behaviours;

import java.util.HashMap;
import java.util.Map;

public class BContext {

    private Map<BKeyType, Object> pairs;

    public BContext(){
        this.pairs = new HashMap<>();
    }

    public boolean containsKeys(BKeyType... keys){
        if (keys == null || keys.length == 0)
            return false;

        for (BKeyType key : keys) {
            //if the key isn't in the context, return false
            if (!(pairs.containsKey(key) && pairs.get(key).getClass().equals(key.getType())))
                return false;
        }
        return true;
    }

    public void addPair(BKeyType key, Object value){
        this.pairs.put(key, value);
    }

    public Object getValue(BKeyType key){
        return pairs.getOrDefault(key, null);
    }
}
