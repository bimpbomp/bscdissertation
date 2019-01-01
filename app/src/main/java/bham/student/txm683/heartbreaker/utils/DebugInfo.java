package bham.student.txm683.heartbreaker.utils;

import java.util.concurrent.ConcurrentHashMap;

public class DebugInfo {

    private ConcurrentHashMap<String, String> infoMap;

    public DebugInfo(){
        infoMap = new ConcurrentHashMap<>();
    }

    public void addValue(String key, String value){
        infoMap.put(key, value);
    }

    public void removeValue(String key){
        infoMap.remove(key);
    }

    public void clearMap(){
        infoMap.clear();
    }

    public boolean isPresent(String key){
        return infoMap.containsKey(key);
    }

    public String[] getValues(){
        return infoMap.values().toArray(new String[0]);
    }
}
