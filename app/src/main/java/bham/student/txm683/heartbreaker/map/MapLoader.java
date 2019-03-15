package bham.student.txm683.heartbreaker.map;

import android.util.Log;
import bham.student.txm683.heartbreaker.MainActivity;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.ai.Drone;
import bham.student.txm683.heartbreaker.ai.Turret;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Portal;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
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

        map.setPlayer(Player.build(jsonObject.getJSONObject("player"), map.getTileSize()));

        map.setPickups(parsePickups(jsonObject.getJSONArray("pickups")));

        map.setPortal(Portal.build(jsonObject.getJSONObject("portal"), map.getTileSize()));

        List<AIEntity> enemies = new ArrayList<>();
        if (jsonObject.has("drones"))
            enemies.addAll(parseEnemies(jsonObject.getJSONArray("drones"), "drones"));

        if (jsonObject.has("turrets"))
            enemies.addAll(parseEnemies(jsonObject.getJSONArray("turrets"), "turrets"));

        if (jsonObject.has("core"))
            enemies.add(Core.build(jsonObject.getJSONObject("core"), map.getTileSize()));

        map.setEnemies(enemies);

        List<DoorBuilder> doorBuilders = new ArrayList<>();

        if (jsonObject.has("doors"))
            doorBuilders = parseDoors(jsonObject.getJSONArray("doors"));

        MapConstructor mapConstructor = new MapConstructor(mainActivity, map);
        mapConstructor.loadMap(doorBuilders);

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

    /*private List<Wall> parseWalls(JSONArray jsonArray) throws JSONException{
        List<Wall> walls = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            walls.add(Wall.build(jsonArray.getJSONObject(i)));
        }

        return walls;
    }

    private Graph<Integer> parseMeshGraph(JSONArray graphArray) throws JSONException{
        Graph<Integer> graph = new Graph<>();

        //create nodes
        for (int nodeIdx = 0; nodeIdx < graphArray.length(); nodeIdx++){
            JSONObject jsonObject = graphArray.getJSONObject(nodeIdx);

            graph.addNode(jsonObject.getInt("id"));
        }

        //create edges
        for (int nodeIdx = 0; nodeIdx < graphArray.length(); nodeIdx++){
            JSONObject jsonObject = graphArray.getJSONObject(nodeIdx);

            JSONArray edges = jsonObject.getJSONArray("edges");
            for (int edgeIdx = 0; edgeIdx < edges.length(); edgeIdx++){
                graph.addConnection(jsonObject.getInt("id"), edges.getInt(edgeIdx));
            }
        }

        return graph;
    }

    private List<MeshPolygon> parseMeshPolygons(JSONArray meshArray) throws JSONException{
        List<MeshPolygon> polygons = new ArrayList<>();

        for (int polyIdx = 0; polyIdx < meshArray.length(); polyIdx++){
            JSONObject polygon = meshArray.getJSONObject(polyIdx);

            List<Point> vertices = new ArrayList<>();

            JSONArray jsonVertices = polygon.getJSONArray("vertices");

            for (int vIdx = 0; vIdx < jsonVertices.length(); vIdx++){
                vertices.add(new Point(jsonVertices.getJSONObject(vIdx)));
            }

            if (vertices.size() != 2)
                throw new JSONException("Incorrect number of vertices in polygon array " + polyIdx + ": have " + vertices.size() + ", expected 2");
            else {
                polygons.add(new MeshPolygon(polygon.getInt("id"),
                        new Rectangle(new Point(polygon.getJSONObject("center")),
                                vertices.get(0),
                                vertices.get(1),
                                ColorScheme.randomColor())
                ));
            }
        }

        return polygons;
    }*/
}
