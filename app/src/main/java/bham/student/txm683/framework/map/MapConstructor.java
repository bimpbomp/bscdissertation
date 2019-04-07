package bham.student.txm683.framework.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import bham.student.txm683.framework.entities.Door;
import bham.student.txm683.framework.entities.Wall;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Tile;
import bham.student.txm683.framework.utils.UniqueID;
import bham.student.txm683.framework.utils.exceptions.MapConversionException;
import bham.student.txm683.framework.utils.graph.Edge;
import bham.student.txm683.heartbreaker.rendering.ColorScheme;

import java.util.*;

public class MapConstructor {
    private int tileSize;

    private UniqueID uniqueID;

    private Queue<Tile> wallsToCheck;

    private IMap map;

    private MeshConstructor meshConstructor;
    private MapReader mapReader;

    public MapConstructor(Context context, IMap map){
        this.uniqueID = new UniqueID();
        this.wallsToCheck = new LinkedList<>();
        this.meshConstructor = new MeshConstructor();
        this.mapReader = new MapReader(context);

        this.map = map;
    }

    @SuppressLint("UseSparseArrays")
    public void loadMap(List<DoorBuilder> doorBuilders){
        this.tileSize = map.getTileSize();

        try {
            mapReader.loadMap(map.getName(), map.getStage(), tileSize, doorBuilders);

            meshConstructor.constructMesh(mapReader.getMeshGenList(), doorBuilders, tileSize);

            List<MeshPolygon> meshPolygons = meshConstructor.getMeshPolygons();

            Log.d("MESHPRINT", "number of nodes: " + meshPolygons.size() + ", number of empty tiles: " + mapReader.getCount());

            printTileList(mapReader.getMeshGenList());

            //GenerateBoundaryWalls
            List<Wall> walls = generateWallsV2(mapReader.getMeshGenList());

            map.setWalls(walls);
            map.setWidthInTiles(mapReader.getWidth());
            map.setHeightInTiles(mapReader.getHeight());

            map.setMeshGraph(meshConstructor.getMeshGraph());
            map.setRootMeshPolygons(meshPolygons);

            constructDoors();

        } catch (MapConversionException e){
            Log.d("MapConstructor", "Map conversion error: " + e.getMessage() + "...\n");
        }
    }

    private void constructDoors(){
        List<Door> doors = new ArrayList<>();

        if (mapReader.getDoorSpawns().size() == 0){
            return;
        }
        for (DoorBuilder doorBuilder : mapReader.getDoorSpawns()){

            Tile sideSets = null;
            int doorSet = 0;

            for (MeshPolygon meshSet : map.getRootMeshPolygons().values()){

                if (meshSet.getBoundingBox().intersecting(doorBuilder.getCenter())){
                    //if the door is in this polygon


                    doorSet = meshSet.getId();
                    List<Edge<Integer>> neighbours = meshConstructor.getMeshGraph().getNode(doorSet).getConnections();

                    //if the door polygon has more than two neighbours, it is not a valid door configuration
                    //as a door has to have exactly two sides open in order to pass through
                    //more than two sides open means the door is pointless as it can be circumvented
                    if (neighbours.size() == 2) {
                        sideSets = new Tile(neighbours.get(0).traverse().getNodeID(),
                                neighbours.get(1).traverse().getNodeID());
                    }
                }
            }

            //if the polygons on either side of the door could be determined,
            //create the door object
            if (sideSets != null && doorSet != 0) {
                Door door = new Door(doorBuilder.getName(), doorBuilder.getCenter(), tileSize, tileSize,
                        doorBuilder.isLocked(), doorBuilder.isVertical(), ColorScheme.DOOR_COLOR, doorSet, sideSets);
                doors.add(door);
            }
        }

        //initialise meshgraph with door states
        for (Door door : doors){
            Tile sideSets = door.getSideSets();

            if (door.isLocked()){
                meshConstructor.getMeshGraph().removeConnection(sideSets.getX(), door.getDoorSet());
                meshConstructor.getMeshGraph().removeConnection(sideSets.getY(), door.getDoorSet());
            } else {
                meshConstructor.getMeshGraph().addConnection(sideSets.getX(), door.getDoorSet());
                meshConstructor.getMeshGraph().addConnection(sideSets.getY(), door.getDoorSet());
            }
        }

        map.setDoors(doors);
    }

    private void printTileList(List<List<Integer>> tileList){
        StringBuilder stringBuilder = new StringBuilder();

        for (List<Integer> row : tileList){

            for (int cell : row){
                stringBuilder.append(cell);
                stringBuilder.append(", ");
            }
            stringBuilder.append("ENDOFROW\n");
        }

        Log.d("MESHPRINT GRID", stringBuilder.toString());
    }

    private List<Wall> generateWallsV2(List<List<Integer>> tileList){

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
                //the wall has at least one vertical neighbour,
                //walk in both vertical directions adding all wall cells found to the currentWall
                currentWall.addAll(walkInDirection(currentTile, new Tile(0, 1), tileList));
                currentWall.addAll(walkInDirection(currentTile, new Tile(0, -1), tileList));

                //sort by y coordinate so the top and bottom coordinates can be determined
                currentWall.sort(Comparator.comparingInt(Tile::getY));

            } else if (neighbours.get(1) != null || neighbours.get(3) != null){
                //the wall has at least one horizontal neighbour,
                //walk in both horizontal directions adding all wall cells found to the currentWall
                currentWall.addAll(walkInDirection(currentTile, new Tile(1, 0), tileList));
                currentWall.addAll(walkInDirection(currentTile, new Tile(-1, 0), tileList));

                //sort by x coordinate so the top and bottom coordinates can be determined
                currentWall.sort(Comparator.comparingInt(Tile::getX));
            }

            //create the wall using the cells contained in currentWall
            //passing the first and last cells as parameters to define the top left and bottom right
            //of the wall.
            //as the coordinates reference the top left corner of a cell, the last cell needs to be incremented
            //in order to meet the bottom right point of the cell
            walls.add(createWall(new Point(currentWall.get(0)), new Point(currentWall.get(currentWall.size()-1).add(1,1))));

            //remove any checked wall tiles from the wallsToCheck queue
            for (Tile tile : currentWall){
                wallsToCheck.remove(tile);
            }
        }

        return walls;
    }

    private static <T> String listToString(List<T> list){
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