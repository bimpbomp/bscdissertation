package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.EnemyType;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomEdge;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomGraph;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.TileBFS;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.*;

public class MapConstructor {
    private static final String TAG = "hb::MapConstructor";
    private Map map;
    private int tileSize;
    private Point centerOffset;

    public MapConstructor(){
    }

    public Map loadMap(String name, int tileSize){
        map = new Map(name, tileSize);
        this.tileSize = tileSize;

        this.centerOffset = new Point(tileSize/2f, tileSize/2f);

        loadTestMap();

        return map;
    }

    @SuppressLint("UseSparseArrays")
    private void loadTestMap(){

        ArrayList<Perimeter> perimeters = new ArrayList<>();

        perimeters.add(new Perimeter(new Point[]{
                new Point(0,0),
                new Point(5,0),
                new Point(5, 5),
                new Point(0, 5)
        }, Color.rgb(102, 255, 0)));

        perimeters.add(new Perimeter(new Point[]{
                new Point(4,1),
                new Point(8,1),
                new Point(8,11),
                new Point(4,11)
        }, Color.rgb(79, 255, 176)));

        perimeters.add(new Perimeter(new Point[]{
                new Point(7,0),
                new Point(12,0),
                new Point(12,6),
                new Point(7,6)
        }, Color.rgb(173, 255, 47)));

        perimeters.add(new Perimeter(new Point[]{
                new Point(1,7),
                new Point(5,7),
                new Point(5,13),
                new Point(1,13)
        }, Color.rgb(80, 200, 120)));

        ArrayList<RoomGrid> roomGrids = new ArrayList<>();
        for (Perimeter perimeter : perimeters){
            roomGrids.add(new RoomGrid(populateGrid(perimeter), tileSize));
        }

        HashMap<Integer, Room> rooms = new HashMap<>();
        int count = 0;
        for (Perimeter perimeter : perimeters){
            perimeter.convertToGlobal(tileSize);
            rooms.put(count, new Room(count, perimeter));
            count++;
        }

        RoomGraph roomGraph = new RoomGraph();

        //initiate room graph and grids
        for (Room room : rooms.values()){
            roomGraph.addNode(room);
        }

        ArrayList<Door> doors = new ArrayList<>();

        doors.add(new Door(0, new Point(4*tileSize,3*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, Color.BLUE));
        doors.add(new Door(1, new Point(7*tileSize,3*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, Color.BLUE));
        doors.add(new Door(2, new Point(4*tileSize,8*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, Color.BLUE));

        RoomEdge door0 = roomGraph.addConnection(0,1, doors.get(0));
        RoomEdge door1 = roomGraph.addConnection(1,2, doors.get(1));
        RoomEdge door2 = roomGraph.addConnection(1,3,  doors.get(2));

        //add doors to roomgrid's tilesets
        roomGrids.get(0).addToTileSet(doors.get(0).getSpawnCoordinates());
        roomGrids.get(1).addToTileSet(doors.get(0).getSpawnCoordinates());

        roomGrids.get(1).addToTileSet(doors.get(1).getSpawnCoordinates());
        roomGrids.get(2).addToTileSet(doors.get(1).getSpawnCoordinates());

        roomGrids.get(1).addToTileSet(doors.get(2).getSpawnCoordinates());
        roomGrids.get(3).addToTileSet(doors.get(2).getSpawnCoordinates());

        door0.getDoor().setTileBackground(tileSize, generateDoorTileColor(door0));
        door1.getDoor().setTileBackground(tileSize, generateDoorTileColor(door1));
        door2.getDoor().setTileBackground(tileSize, generateDoorTileColor(door2));

        //generate all boundary locations
        HashSet<Point> staticEntities = new HashSet<>();
        staticEntities.addAll(generateBoundaries(rooms.get(0).getPerimeter()));
        staticEntities.addAll(generateBoundaries(rooms.get(1).getPerimeter()));
        staticEntities.addAll(generateBoundaries(rooms.get(2).getPerimeter()));
        staticEntities.addAll(generateBoundaries(rooms.get(3).getPerimeter()));

        //remove any boundary walls on same tile as doors
        for (Door door : doors){
            staticEntities.remove(door.getSpawnCoordinates());
        }

        for (RoomGrid roomGrid : roomGrids){
            StringBuilder stringBuilder = new StringBuilder();
            for (Tile tile : roomGrid.getTileSet()){
                stringBuilder.append(tile.toString());
                stringBuilder.append(", ");
            }
            Log.d("hb::GRIDS", stringBuilder.toString());
        }

        ArrayList<Pair<EnemyType, Point>> enemies = new ArrayList<>();
        enemies.add(new Pair<>(EnemyType.CHASER, new Point(10*tileSize,tileSize)));

        //initialise map with generated contents
        map.setWidthInTiles(12);
        map.setHeightInTiles(13);
        map.setRoomGraph(roomGraph);
        map.setRooms(rooms);
        map.setDoors(doors);
        map.setPlayerSpawn(new Point(tileSize, tileSize).add(centerOffset));
        map.setStaticEntities(new ArrayList<>(staticEntities));
        map.setEnemies(enemies);
    }

    private int generateDoorTileColor(RoomEdge edge){

        int firstColor = edge.getConnectedRoomNodes().first.getRoom().getPerimeter().getDefaultColor();
        int secondColor = edge.getConnectedRoomNodes().second.getRoom().getPerimeter().getDefaultColor();

        int red = (Color.red(firstColor)+Color.red(secondColor))/2;
        int green = (Color.green(firstColor)+Color.green(secondColor))/2;
        int blue = (Color.blue(firstColor)+Color.blue(secondColor))/2;

        return Color.rgb(red, green, blue);
    }

    private List<Point> generateBoundaries(Perimeter perimeter){
        ArrayList<Point> boundaries = new ArrayList<>();

        Point[] vertices = perimeter.getCollisionVertices();

        Point lastBoundaryTile = vertices[0];
        for (int i = 0; i < vertices.length - 1; i++){
            //generates the boundaries between the current two vertices
            boundaries.addAll(generateBoundary(vertices[i], vertices[i+1], lastBoundaryTile));
            lastBoundaryTile = boundaries.get(boundaries.size()-1).add(centerOffset.smult(-1));
        }
        boundaries.addAll(generateBoundary(vertices[vertices.length-1], vertices[0], lastBoundaryTile));

        return boundaries;
    }

    private List<Point> generateBoundary(Point startVertex, Point endVertex, Point lastPlacedTile){
        ArrayList<Point> boundary = new ArrayList<>();

        Vector directionToNextVertex = new Vector(map.mapGlobalPointToTilePoint(startVertex), map.mapGlobalPointToTilePoint(endVertex));
        int tilesToAdd = (int) directionToNextVertex.getLength();

        Point currentPoint = lastPlacedTile;

        while (tilesToAdd > 0){
            boundary.add(currentPoint.add(centerOffset));

            currentPoint = currentPoint.add(directionToNextVertex.getUnitVector().getRelativeToTailPoint().smult(tileSize));
            tilesToAdd--;
        }

        return boundary;
    }

    public static ArrayList<Tile> populateGrid(Perimeter perimeter) {
        ArrayList<Tile> tileSet = new ArrayList<>();

        //visited tiles are added here
        HashSet<Tile> closedSet = new HashSet<>();

        Tile startingPosition = new Tile((int)perimeter.getCollisionVertices()[0].getX(),(int)perimeter.getCollisionVertices()[0].getY());

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        PriorityQueue<Pair<Tile, Integer>> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
                return 0;
            return 1; });
        openSet.add(new Pair<>(startingPosition, 0));

        //initialise variables needed in loop
        Pair<Tile, Integer> tileAndCost;
        Tile currentTile;
        int currentCost;
        while (!openSet.isEmpty()) {

            //get the tile with the lowest cost
            tileAndCost = openSet.poll();
            currentTile = tileAndCost.first;
            currentCost = tileAndCost.second;

            //add it to the closed set so it isn't inspected again
            closedSet.add(currentTile);

            ArrayList<Tile> neighbours = TileBFS.getNeighbours(currentTile);

            for (Tile neighbour : neighbours) {

                if (closedSet.contains(neighbour) || !tileIsInPerimeter(perimeter, neighbour)) {
                    //if the tile is out of bounds, or has already been inspected, move on
                    continue;
                }

                //neighbour is a valid tile but not target, calc it's cost and add to openset
                int neighbourCost = currentCost + 1;
                openSet.add(new Pair<>(neighbour, neighbourCost));

                tileSet.add(neighbour);
            }
        }

        return tileSet;
    }

    private static boolean tileIsInPerimeter(Perimeter perimeter, Tile tile){

        if (perimeter.getCollisionVertices().length > 3) {
            Point topLeft = perimeter.getCollisionVertices()[0].add(new Point(1,1));
            Point bottomRight = perimeter.getCollisionVertices()[2].add(new Point(-1,-1));

            return (tile.getX() >= topLeft.getX()) && (tile.getY() >= topLeft.getY())
                    && (tile.getX() < bottomRight.getX()) && (tile.getY() < bottomRight.getY());
        }
        return false;
    }

    /*private Tile searchForTileType(Tile startingPosition, int desiredTileType) throws MapConversionException {
        *//*if (!TileType.isValidTileType(desiredTileType))
            throw new MapConversionException(MCEReason.SEARCH_FOR_INVALID_TILE_TYPE);*//*

        //visited tiles are added here
        HashSet<Tile> closedSet = new HashSet<>();

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        PriorityQueue<Pair<Tile, Integer>> openSet = TileBFS.initOpenSet();
        openSet.add(new Pair<>(startingPosition, 0));

        //initialise variables needed in loop
        Pair<Tile, Integer> tileAndCost;
        Tile currentTile;
        int currentCost;
        int neighbourTileType;
        while (!openSet.isEmpty()) {

            //get the tile with the lowest cost
            tileAndCost = openSet.poll();
            currentTile = tileAndCost.first;
            currentCost = tileAndCost.second;

            //add it to the closed set so it isn't inspected again
            closedSet.add(currentTile);

            ArrayList<Tile> neighbours = TileBFS.getNeighbours(currentTile);

            for (Tile neighbour : neighbours) {
                //get the color of this neighbour
                //neighbourTileType = getPixelColorWithOffset(neighbour,0,0);
                neighbourTileType = currentTile.getX();

                *//*if (neighbourTileType == TileType.INVALID || closedSet.contains(neighbour)) {
                    //if the tile is out of bounds, or has already been inspected, move on
                    continue;
                } else if (neighbourTileType == desiredTileType) {
                    //if the tile is the goal, return
                    return neighbour;
                }*//*

                //neighbour is a valid tile but not target, calc it's cost and add to openset
                int neighbourCost = currentCost + 1;
                openSet.add(new Pair<>(neighbour, neighbourCost));
            }
        }
        return startingPosition;
    }*/
}
