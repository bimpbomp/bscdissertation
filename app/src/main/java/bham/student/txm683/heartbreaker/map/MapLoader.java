package bham.student.txm683.heartbreaker.map;

import android.util.Log;
import bham.student.txm683.framework.map.DoorBuilder;
import bham.student.txm683.framework.map.MapConstructor;
import bham.student.txm683.framework.utils.Tile;
import bham.student.txm683.framework.utils.UniqueID;
import bham.student.txm683.heartbreaker.MainActivity;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Drone;
import bham.student.txm683.heartbreaker.ai.Overlord;
import bham.student.txm683.heartbreaker.ai.Turret;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapLoader {

    private MainActivity mainActivity;
    private Map map;

    public MapLoader(String mapName, String stage, int tileSize, MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        this.map = new Map(mapName, stage, tileSize);
    }

    public Map loadMap() throws JSONException {
        String mapString;
        try {
            InputStream inputStream = mainActivity.getAssets().open(String.format(Locale.UK, "maps/%s/%s_%s.json",
                    map.getName(), map.getName(), map.getStage()));

            mapString = mainActivity.readFromFile(inputStream);

        } catch (IOException e){
            Log.d("MAPLOADER", "IOEXCEPTION reading in map file: " + e.getMessage());
            return null;
        }

        JSONObject jsonObject = new JSONObject(mapString);

        if (jsonObject.has("player"))
            map.setPlayer(Player.build(jsonObject.getJSONObject("player"), map.getTileSize()));
        else
            throw new JSONException("no player found in json file");

        if (jsonObject.has("pickups"))
            map.setPickups(parsePickups(jsonObject.getJSONArray("pickups")));
        else
            map.setPickups(new ArrayList<>());

        List<AIEntity> enemies = new ArrayList<>();
        if (jsonObject.has("drones"))
            enemies.addAll(parseEnemies(jsonObject.getJSONArray("drones"), "drones"));

        if (jsonObject.has("turrets"))
            enemies.addAll(parseEnemies(jsonObject.getJSONArray("turrets"), "turrets"));

        map.setEnemies(enemies);

        List<DoorBuilder> doorBuilders = new ArrayList<>();

        if (jsonObject.has("doors"))
            doorBuilders = parseDoors(jsonObject.getJSONArray("doors"));

        MapConstructor mapConstructor = new MapConstructor(mainActivity, map);
        mapConstructor.loadMap(doorBuilders);

        List<Overlord> overlords = new ArrayList<>();

        if (jsonObject.has("overlords")) {
            JSONArray jsonArray = jsonObject.getJSONArray("overlords");
            for (int i = 0; i < jsonArray.length(); i++) {
                overlords.add(new Overlord(jsonArray.getJSONObject(i), map.getTileSize()));
            }
        }

        map.setOverlords(overlords);

        return map;
    }

    private List<DoorBuilder> parseDoors(JSONArray jsonArray) throws  JSONException {
        List<DoorBuilder> doorBuilders = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String name = jsonObject.getString("name");
            Tile tile = new Tile(jsonObject.getJSONObject("tile"));
            boolean locked = jsonObject.getBoolean("locked");

            doorBuilders.add(new DoorBuilder(name, tile, locked));

            Log.d("LOADING", name + " added to doorbuilder list in parseDoors...");
        }

        return doorBuilders;
    }

    private List<Pickup> parsePickups(JSONArray jsonArray) throws JSONException{
        UniqueID uniqueID = new UniqueID();

        List<Pickup> pickups = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            pickups.add(Pickup.build(jsonArray.getJSONObject(i), uniqueID.id(), map.getTileSize()));
        }

        return pickups;
    }

    private List<AIEntity> parseEnemies(JSONArray jsonArray, String type) throws JSONException {
        List<AIEntity> enemies = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            AIEntity enemy = null;
            JSONObject item = jsonArray.getJSONObject(i);

            switch (type){
                case "drones":
                    enemy = Drone.build(item, map.getTileSize());
                    break;
                case "turrets":
                    enemy = Turret.build(item, map.getTileSize());
                    break;
                default:
                    break;
            }
            if (enemy != null)
                enemies.add(enemy);
        }
        return enemies;
    }
}
