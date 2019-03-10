package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.ai.Drone;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapLoader {

    private static final String TEST_STRING = "{\n" +
            "  \"graph\": [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"edges\": [\n" +
            "        2,\n" +
            "        3,\n" +
            "        4\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"edges\": [\n" +
            "        1,\n" +
            "        3\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 3,\n" +
            "      \"edges\": [\n" +
            "        2,\n" +
            "        3\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 4,\n" +
            "      \"edges\": [\n" +
            "        1,\n" +
            "        2,\n" +
            "        3\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"mesh\": [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"center\": {\n" +
            "        \"x\": 2,\n" +
            "        \"y\": 3\n" +
            "      },\n" +
            "      \"vertices\": [\n" +
            "        {\n" +
            "          \"x\": 1,\n" +
            "          \"y\": 3\n" +
            "        },\n" +
            "        {\n" +
            "          \"x\": 3,\n" +
            "          \"y\": 3\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"center\": {\n" +
            "        \"x\": 3,\n" +
            "        \"y\": 4\n" +
            "      },\n" +
            "      \"vertices\": [\n" +
            "        {\n" +
            "          \"x\": 1,\n" +
            "          \"y\": 3\n" +
            "        },\n" +
            "        {\n" +
            "          \"x\": 3,\n" +
            "          \"y\": 3\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 3,\n" +
            "      \"center\": {\n" +
            "        \"x\": 4,\n" +
            "        \"y\": 5\n" +
            "      },\n" +
            "      \"vertices\": [\n" +
            "        {\n" +
            "          \"x\": 1,\n" +
            "          \"y\": 3\n" +
            "        },\n" +
            "        {\n" +
            "          \"x\": 3,\n" +
            "          \"y\": 3\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 4,\n" +
            "      \"center\": {\n" +
            "        \"x\": 6,\n" +
            "        \"y\": 6\n" +
            "      },\n" +
            "      \"vertices\": [\n" +
            "        {\n" +
            "          \"x\": 1,\n" +
            "          \"y\": 3\n" +
            "        },\n" +
            "        {\n" +
            "          \"x\": 3,\n" +
            "          \"y\": 3\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public MapLoader() {
    }

    public Map loadMap(String mapString) throws JSONException {
        Map map = new Map();
        //TODO insert read from file logic here

        JSONObject jsonObject = new JSONObject(TEST_STRING);

        //parse graph
        map.setMeshGraph(parseMeshGraph(jsonObject.getJSONArray("graph")));

        //parse mesh polygons
        map.setRootMeshPolygons(parseMeshPolygons(jsonObject.getJSONArray("mesh")));

        map.setDimInTiles(jsonObject.getInt("width"), jsonObject.getInt("height"));

        map.setPlayer(Player.build(jsonObject.getJSONObject("player")));

        map.setCore(Core.build(jsonObject.getJSONObject("core")));

        map.setPickups(parsePickups(jsonObject.getJSONArray("pickups")));

        map.setEnemies(parseEnemies(jsonObject.getJSONArray("enemies")));

        map.setDoors(parseDoors(jsonObject.getJSONArray("doors")));
        map.setWalls(parseWalls(jsonObject.getJSONArray("walls")));

        return map;
    }

    private List<Wall> parseWalls(JSONArray jsonArray) throws JSONException{
        List<Wall> walls = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            walls.add(Wall.build(jsonArray.getJSONObject(i)));
        }

        return walls;
    }

    private List<Door> parseDoors(JSONArray jsonArray) throws JSONException {
        List<Door> doors = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            doors.add(Door.build(jsonArray.getJSONObject(i)));
        }

        return doors;
    }

    private List<Pickup> parsePickups(JSONArray jsonArray) throws JSONException{
        List<Pickup> pickups = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            pickups.add(Pickup.build(jsonArray.getJSONObject(i)));
        }

        return pickups;
    }

    public List<AIEntity> parseEnemies(JSONArray jsonArray) throws JSONException {
        List<AIEntity> enemies = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            AIEntity enemy;
            JSONObject item = jsonArray.getJSONObject(i);

            switch (item.getString("type")){
                case "drone":
                    enemy = Drone.build(item.getJSONObject("fields"));
                    break;
                case "turret":
                    enemy = Drone.build(item.getJSONObject("fields"));
                    break;
                default:
                    throw new JSONException("AI type is not recognised: " + item.getString("type"));
            }
            enemies.add(enemy);
        }
        return enemies;
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
    }
}
