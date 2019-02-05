package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.EnemyType;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomGraph;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
        RoomGraph roomGraph = new RoomGraph();
        //initiate room graph
        roomGraph.addNode(0);
        roomGraph.addNode(1);
        roomGraph.addNode(2);
        roomGraph.addNode(3);

        roomGraph.addConnection(0,1, 0);
        roomGraph.addConnection(1,2, 1);
        roomGraph.addConnection(1,3, 2);

        HashMap<Integer, Perimeter> roomPerimeters = new HashMap<>();
        roomPerimeters.put(0, new Perimeter(new Point[]{
                new Point(0,0),
                new Point(5*tileSize,0),
                new Point(5*tileSize, 5*tileSize),
                new Point(0, 5*tileSize)
        }, Color.rgb(102, 255, 0)));

        roomPerimeters.put(1, new Perimeter(new Point[]{
                new Point(4*tileSize,tileSize),
                new Point(8*tileSize,tileSize),
                new Point(8*tileSize,11*tileSize),
                new Point(4*tileSize,11*tileSize)
        }, Color.rgb(79, 255, 176)));

        roomPerimeters.put(2, new Perimeter(new Point[]{
                new Point(7*tileSize,0),
                new Point(12*tileSize,0),
                new Point(12*tileSize,6*tileSize),
                new Point(7*tileSize,6*tileSize)
        }, Color.rgb(173, 255, 47)));

        roomPerimeters.put(3, new Perimeter(new Point[]{
                new Point(tileSize,7*tileSize),
                new Point(5*tileSize,7*tileSize),
                new Point(5*tileSize,13*tileSize),
                new Point(tileSize,13*tileSize)
        }, Color.rgb(80, 200, 120)));

        //generate all boundary locations
        HashSet<Point> staticEntities = new HashSet<>();
        staticEntities.addAll(generateBoundaries(roomPerimeters.get(0)));
        staticEntities.addAll(generateBoundaries(roomPerimeters.get(1)));
        staticEntities.addAll(generateBoundaries(roomPerimeters.get(2)));
        staticEntities.addAll(generateBoundaries(roomPerimeters.get(3)));

        ArrayList<Door> doors = new ArrayList<>();
        doors.add(new Door(0, new Point(4*tileSize,3*tileSize).add(centerOffset), tileSize/2, tileSize, false, Color.BLUE));
        doors.add(new Door(1, new Point(7*tileSize,3*tileSize).add(centerOffset), tileSize/2, tileSize, false, Color.BLUE));
        doors.add(new Door(2, new Point(4*tileSize,8*tileSize).add(centerOffset), tileSize/2, tileSize, false, Color.BLUE));

        //remove any boundary walls on same tile as doors
        for (Door door : doors){
            staticEntities.remove(door.getSpawnCoordinates());
        }

        ArrayList<Pair<EnemyType, Point>> enemies = new ArrayList<>();
        enemies.add(new Pair<>(EnemyType.CHASER, new Point(10*tileSize,tileSize)));

        //initialise map with generated contents
        map.setWidthInTiles(12);
        map.setHeightInTiles(13);
        map.setRoomGraph(roomGraph);
        map.setRoomPerimeters(roomPerimeters);
        map.setDoors(doors);
        map.setPlayerSpawn(new Point(tileSize, tileSize).add(centerOffset));
        map.setStaticEntities(new ArrayList<>(staticEntities));
        map.setEnemies(enemies);
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
