package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import bham.student.txm683.heartbreaker.TileSet;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.ai.Drone;
import bham.student.txm683.heartbreaker.ai.Turret;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.pickups.Key;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.graph.Edge;

import java.util.*;

public class MapConstructor {
    private static final String TAG = "hb::MapConstructor";
    private Map map;
    private int tileSize;
    private Point centerOffset;

    private UniqueID uniqueID;

    private Queue<Tile> wallsToCheck;

    //hardcoded mesh tileList for now till algorithm implemented
    public static List<List<Integer>> tileList = new ArrayList<>();
    static {
        tileList.add(Arrays.asList(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,-1,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,-1,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,-1,-1,-1,-1,-1,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,-1,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1));
    }

    public MapConstructor(){
        this.uniqueID = new UniqueID();
        this.wallsToCheck = new LinkedList<>();
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

        /*
         * Specify Map Layout
         */

        int mapWidth = 10;
        int mapHeight = 10;

        ArrayList<Perimeter> perimeters = new ArrayList<>();
        ArrayList<Door> doors = new ArrayList<>();
        List<Pickup> pickups = new ArrayList<>();
        ArrayList<AIEntity> enemies = new ArrayList<>();

        Player player;
        Core core;

        perimeters.add(new Perimeter(new Point[]{
                new Point(0,0),
                new Point(10,0),
                new Point(10,10),
                new Point(0,10)
        }, Color.GREEN));

        player = new Player("player", new Point(2*tileSize,2*tileSize), tileSize/2, tileSize*3,
                ColorScheme.UPPER_PLAYER_COLOR, ColorScheme.LOWER_PLAYER_COLOR, 100);

        enemies.add(new Drone("DRONE"+uniqueID.id(), new Point(5*tileSize, 2*tileSize).add(centerOffset), tileSize/2,
                ColorScheme.CHASER_COLOR, tileSize*1.5f, 100));

        enemies.add(new Turret("TURRET"+ uniqueID.id(), getTileCenter(5,3), tileSize/2, ColorScheme.CHASER_COLOR, 100));

        core = new Core("core", new Point(8*tileSize,8*tileSize).add(centerOffset), tileSize/2);

        /*
         * Generate Map
         */

        MeshConstructorV2 meshConstructor = new MeshConstructorV2();
        meshConstructor.constructMesh(tileList);

        //convert perimeter coordinates to global with tileSize and add them to a room
        HashMap<Integer, Room> rooms = new HashMap<>();
        int count = 0;
        for (Perimeter perimeter : perimeters){
            perimeter.convertToGlobal(tileSize);
            rooms.put(count, new Room(count, perimeter));
            count++;
        }

        //GenerateBoundaryWalls
        List<Wall> walls = generateWallsV2(tileList);

        //add all statics to the tileset
        TileSet tileSet = new TileSet(tileSize);

        for (Wall wall : walls){
            tileSet.addPermanentToGrid(wall);
        }

        for (Door door : doors){
            tileSet.addPermanentToGrid(door);
        }

        Tile sideSets = null;
        int doorSet = 0;
        for (MeshSet meshSet : meshConstructor.getMeshIntersectionSets()){
            if (meshSet.getContainedTiles().contains(new Tile(8,5))){
                Log.d("hb::HASDOORTILE", meshSet.getId()+"");
                doorSet = meshSet.getId();

                List<Edge<Integer>> neighbours = meshConstructor.getMeshGraph().getNode(doorSet).getConnections();

                sideSets = new Tile(neighbours.get(0).traverse().getNodeID(),
                                neighbours.get(1).traverse().getNodeID());
            }
        }

        if (sideSets != null && doorSet != 0) {
            doors.add(new Door(uniqueID.id(), getTileCenter(8,5), tileSize, tileSize,
                    true, false, ColorScheme.DOOR_COLOR, doorSet, sideSets));

            pickups.add(new Key("K"+uniqueID.id(), doors.get(0).getName(),
                    getTileCenter(6,8), tileSize/4));
        }


        //initialise meshgraph with door states
        for (Door door : doors){
            sideSets = door.getSideSets();

            if (door.isLocked()){
                meshConstructor.getMeshGraph().removeConnection(sideSets.getX(), door.getDoorSet());
                meshConstructor.getMeshGraph().removeConnection(sideSets.getY(), door.getDoorSet());
            } else {
                meshConstructor.getMeshGraph().addConnection(sideSets.getX(), door.getDoorSet());
                meshConstructor.getMeshGraph().addConnection(sideSets.getY(), door.getDoorSet());
            }
        }

        //reset map object
        map.setTileSet(tileSet);
        map.setWalls(walls);
        map.setWidthInTiles(mapWidth);
        map.setHeightInTiles(mapHeight);

        map.setRooms(rooms);
        map.setDoors(doors);
        map.setPlayer(player);

        map.setEnemies(enemies);
        map.setPickups(pickups);
        map.setCore(core);

        map.setMeshGraph(meshConstructor.getMeshGraph());
        map.setRootMeshPolygons(meshConstructor.getMeshPolygons(tileSize));
    }

    private Point getTileCenter(int x, int y){
        return new Point(x*tileSize, y*tileSize).add(centerOffset);
    }

    public List<Wall> generateWallsV2(List<List<Integer>> tileList){

        List<Wall> walls = new ArrayList<>();

        wallsToCheck = new LinkedList<>(findWallTiles(tileList));

        Tile currentTile;

        List<Tile> currentWall;
        while (!wallsToCheck.isEmpty()){
            currentTile = wallsToCheck.poll();

            currentWall = new ArrayList<>();
            currentWall.add(currentTile);

            List<Tile> neighbours = getNeighbours(currentTile, tileList);

            if (neighbours.get(0) != null || neighbours.get(2) != null){
                currentWall.addAll(walkInDirection(currentTile, new Tile(0, 1), tileList));
                currentWall.addAll(walkInDirection(currentTile, new Tile(0, -1), tileList));
                currentWall.sort((a,b) -> {
                    if (a.getY() < b.getY())
                        return -1;
                    else if (a.getY() > b.getY())
                        return 1;
                    else
                        return 0;
                });

            } else if (neighbours.get(1) != null || neighbours.get(3) != null){
                currentWall.addAll(walkInDirection(currentTile, new Tile(1, 0), tileList));
                currentWall.addAll(walkInDirection(currentTile, new Tile(-1, 0), tileList));

                currentWall.sort((a,b) -> {
                    if (a.getX() < b.getX())
                        return -1;
                    else if (a.getX() > b.getX())
                        return 1;
                    else
                        return 0;
                });
            }

            Log.d("hb::GenWall", listToString(currentWall));

            walls.add(createWall(new Point(currentWall.get(0)), new Point(currentWall.get(currentWall.size()-1).add(1,1))));

            //remove any checked wall tiles from the wallsToCheck queue
            for (Tile tile : currentWall){
                wallsToCheck.remove(tile);
            }
        }

        return walls;
    }

    public static <T> String listToString(List<T> list){
        StringBuilder stringBuilder = new StringBuilder();

        for (Object object : list){
            stringBuilder.append(object.toString());
            stringBuilder.append(", ");
        }
        stringBuilder.append("END");

        return stringBuilder.toString();
    }

    private Wall createWall(Point topLeft, Point bottomRight){
        topLeft = topLeft.sMult(tileSize);
        bottomRight = bottomRight.sMult(tileSize);

        Point center = topLeft.add((bottomRight.getX()-topLeft.getX())/2f, (bottomRight.getY()-topLeft.getY())/2f);
        Log.d("hb::Wall", topLeft.toString() + ", " + bottomRight.toString() + ", " + center.toString());
        return new Wall("W"+uniqueID.id(), topLeft, bottomRight, center, ColorScheme.WALL_COLOR);
    }

    private List<Tile> walkInDirection(Tile start, Tile direction, List<List<Integer>> tileList){
        List<Tile> wallPieces = new ArrayList<>();

        Tile nextTile = start.add(direction);

        while (isWallTile(nextTile, tileList)){
            wallPieces.add(nextTile);
            nextTile = nextTile.add(direction);
        }

        return wallPieces;
    }

    /**
     * returns 4-way neighbours in a clockwise fashion
     * @param tile
     * @param tileList
     * @return neighbours
     */
    private List<Tile> getNeighbours(Tile tile, List<List<Integer>> tileList){
        List<Tile> list = new ArrayList<>();

        addIfWall(tile.add(0,-1), list, tileList);
        addIfWall(tile.add(1,0), list, tileList);
        addIfWall(tile.add(0,1), list, tileList);
        addIfWall(tile.add(-1,0), list, tileList);

        return list;
    }

    private void addIfWall(Tile x, List<Tile> addToList, List<List<Integer>> tileList){
        if (isWallTile(x, tileList)){
            addToList.add(x);
        } else {
            addToList.add(null);
        }
    }

    private boolean isWallTile(Tile tile, List<List<Integer>> tileList){
        try {
            return (tileList.get(tile.getY()).get(tile.getX()) == -1) && wallsToCheck.contains(tile);
        } catch (IndexOutOfBoundsException e){
            return false;
        }
    }

    private List<Tile> findWallTiles(List<List<Integer>> tileList){
        List<Tile> wallTiles = new ArrayList<>();

        for (int i = 0; i < tileList.size(); i++){
            List<Integer> row = tileList.get(i);

            for (int j = 0; j < row.size(); j++){
                if (row.get(j) == -1){
                    wallTiles.add(new Tile(j, i));
                }
            }
        }
        return wallTiles;
    }
}