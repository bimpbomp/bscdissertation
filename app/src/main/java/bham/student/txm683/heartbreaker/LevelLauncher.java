package bham.student.txm683.heartbreaker;

import android.os.Bundle;

public class LevelLauncher {

    private String mapName;

    public LevelLauncher(){

    }

    public LevelLauncher(Bundle bundle){
        mapName = bundle.getString("map_name", "");

        if (mapName.equals(""))
            throw new IllegalArgumentException("Map name not able to be extracted from bundle");
    }

    public Bundle createBundle(){
        Bundle bundle = new Bundle();

        bundle.putString("map_name", mapName);

        return bundle;
    }

    public void setMapName(String mapName){
        this.mapName = mapName;
    }

    public String getMapName(){
        return this.getMapName();
    }
}