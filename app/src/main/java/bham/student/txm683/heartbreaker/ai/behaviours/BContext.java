package bham.student.txm683.heartbreaker.ai.behaviours;

import java.util.HashMap;
import java.util.Map;

public class BContext {

    private Map<BKeyType, Object> pairs;

    private Map<String, Object> variables;

    public BContext(){
        this.pairs = new HashMap<>();
        this.variables = new HashMap<>();
    }

    public void addVariable(String key, Object value){
        variables.put(key, value);
    }

    public boolean containsVariables(String... keys){
        if (keys == null || keys.length == 0)
            return false;

        for (String key : keys){
            if (!variables.containsKey(key))
                return false;
        }
        return true;
    }

    public Object getVariable(String key){
        return variables.getOrDefault(key, null);
    }

    public boolean containsKeys(BKeyType... keys){
        if (keys == null || keys.length == 0)
            return false;

        for (BKeyType key : keys) {
            //if the key isn't in the context, return false
            if (!pairs.containsKey(key))
                return false;

            Class sclass = pairs.get(key).getClass();
            while (sclass != null && !sclass.equals(Object.class)){

                if (sclass.equals(key.getType()))
                    return true;

                sclass = sclass.getSuperclass();
            }
        }
        return false;
    }

    public void addPair(BKeyType key, Object value){
        this.pairs.put(key, value);
    }

    public void removePair(BKeyType key){
        this.pairs.remove(key);
    }

    public Object getValue(BKeyType key){
        return pairs.getOrDefault(key, null);
    }
}
