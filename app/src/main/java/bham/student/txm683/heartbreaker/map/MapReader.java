package bham.student.txm683.heartbreaker.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.R;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.exceptions.MCEReason;
import bham.student.txm683.heartbreaker.utils.exceptions.MapConversionException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import static bham.student.txm683.heartbreaker.map.MapReader.Direction.*;
import static bham.student.txm683.heartbreaker.map.TileType.INVALID;
import static bham.student.txm683.heartbreaker.map.TileType.*;

public class MapReader {

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        HORIZONTAL,
        VERTICAL,
        INVALID
    }


    private static final String TAG = "hb::MapReader";

    private Context context;
    private Bitmap mapImage;

    private PriorityQueue<Pair<Tile, Integer>> openSet;

    private List<List<Integer>> meshGenList;
    private List<Pair<Integer, Point>> spawnLocations;
    private List<Pair<Point, Boolean>> doorSpawns;

    private int tileSize;
    private Point centerOffset;

    private int width, height;

    public MapReader(Context context){
        this.context = context;
        this.meshGenList = new ArrayList<>();
        this.spawnLocations = new ArrayList<>();
        this.doorSpawns = new ArrayList<>();

        width = 0;
        height = 0;
    }

    public List<List<Integer>> getMeshGenList() {
        return meshGenList;
    }

    public List<Pair<Point, Boolean>> getDoorSpawns(){
        return doorSpawns;
    }

    public List<Pair<Integer, Point>> getSpawnLocations() {
        return spawnLocations;
    }

    public void loadMap(String mapName, int tileSize) throws MapConversionException {
        this.tileSize = tileSize;
        this.centerOffset = new Point(tileSize/2f, tileSize/2f);

        convertMapBitmapToGraph(mapName);
    }


    private int getMapRId(String mapName){

        Field[] fields=R.raw.class.getFields();

        for(Field field : fields){

            if (field.getName().equals(mapName))
                try {
                    return field.getInt(field);
                } catch (IllegalAccessException e){
                    //do nothing
                }
        }
        return -1;
    }

    private void convertMapBitmapToGraph(String mapName) throws MapConversionException {

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        try {
            //mapImage = BitmapFactory.decodeResource(context.getResources(), R.raw.map1, o);
            mapImage = BitmapFactory.decodeStream(context.getAssets().open("maps/"+mapName+".png"));
        } catch (IOException e){
            throw new MapConversionException(MCEReason.FILE_DECODING, e);
        }

        //dimensions in pixels/tiles
        height = mapImage.getHeight();
        width = mapImage.getWidth();

        List<Integer> tileValues;
        for (int rowIdx = 0; rowIdx < height; rowIdx++){
            tileValues = new ArrayList<>();

            for (int columnIdx = 0; columnIdx < width; columnIdx++){

                int pixel = mapImage.getPixel(columnIdx, rowIdx);

                if (isValidTileType(pixel)){

                    if (pixel == WALL){
                        tileValues.add(-1);

                    } else{
                        tileValues.add(0);

                        if (pixel == DOOR){

                            Direction d = findRoomsAdjacentToDoor(new Tile(columnIdx, rowIdx));
                            Log.d("DOORSPAWN", columnIdx + ", " + rowIdx + ": " + d.name());

                            doorSpawns.add(new Pair<>(convertToGlobal(columnIdx, rowIdx), (d == VERTICAL)));
                        } else
                            spawnLocations.add(new Pair<>(pixel, convertToGlobal(columnIdx, rowIdx)));
                    }

                } else {
                    Log.d("MAP_READER", "Invalid tile type at: (" + columnIdx + "," + rowIdx + ")");
                    tileValues.add(0);
                }
            }

            meshGenList.add(tileValues);
        }
    }

    private Point convertToGlobal(int columnIdx, int rowIdx){
        return new Point(columnIdx, rowIdx).sMult(tileSize).add(centerOffset);
    }

    private void roomInitialisation(Tile startingPosition) throws MapConversionException{

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        initOpenSet();

        openSet.add(new Pair<>(startingPosition, 0));

        //visited tiles are added here
        HashSet<Tile> closedSet = new HashSet<>();

        //initialise variables needed in loop
        Pair<Tile, Integer> tileAndCost;
        Tile currentTile;
        int currentColor;

        int currentCost;
        int neighbourTileType;
        while (!openSet.isEmpty()) {

            //get the tile with the lowest cost
            tileAndCost = openSet.poll();
            currentTile = tileAndCost.first;
            currentCost = tileAndCost.second;

            currentColor = getPixelColorWithOffset(currentTile, 0, 0);
            //process the current tile
            switch (currentColor){
                case (PLAYER):
                    break;
                case (DRONE):
                    break;
                case (DOOR):
                    //if door doesn't exist in graph already, add it

                    break;
                case (WALL):
                    break;
                default:
                    continue;
            }

            //add it to the closed set so it isn't inspected again
            closedSet.add(currentTile);

            ArrayList<Tile> neighbours = getNeighbours(currentTile);

            for (Tile neighbour : neighbours) {
                //get the color of this neighbour
                neighbourTileType = getPixelColorWithOffset(neighbour,0,0);

                if (neighbourTileType == INVALID || closedSet.contains(neighbour)) {
                    //if the tile is out of bounds, or has already been inspected, move on
                    continue;
                }

                //neighbour is a valid tile, calc it's cost and add to openset
                int neighbourCost = currentCost + 1;
                openSet.add(new Pair<>(neighbour, neighbourCost));
            }
        }
    }

    //BFS of bitmap for first instance of TileType constant provided.
    //memoryless, doesn't return path or keep track of where it came from.
    //Used for finding a specific tile
    private Tile searchForTileType(Tile startingPosition, int desiredTileType) throws MapConversionException{
        if (!isValidTileType(desiredTileType))
            throw new MapConversionException(MCEReason.SEARCH_FOR_INVALID_TILE_TYPE);

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        initOpenSet();

        //visited tiles are added here
        HashSet<Tile> closedSet = new HashSet<>();

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

            ArrayList<Tile> neighbours = getNeighbours(currentTile);

            for (Tile neighbour : neighbours) {
                //get the color of this neighbour
                neighbourTileType = getPixelColorWithOffset(neighbour,0,0);

                if (neighbourTileType == INVALID || closedSet.contains(neighbour)) {
                    //if the tile is out of bounds, or has already been inspected, move on
                    continue;
                } else if (neighbourTileType == desiredTileType) {
                    //if the tile is the goal, return
                    return neighbour;
                }

                //neighbour is a valid tile but not target, calc it's cost and add to openset
                int neighbourCost = currentCost + 1;
                openSet.add(new Pair<>(neighbour, neighbourCost));
            }
        }

        return startingPosition;
    }

    //gets the coordinates of the 8 surrounding cells. Warning: doesn't check if they're valid
    private ArrayList<Tile> getNeighbours(Tile coordinates){
        ArrayList<Tile> neighbours = new ArrayList<>();

        for (int i = -1; i < 2; i++){
            for (int j = -1; j < 2; j++){
                if (i == 0 && j == 0)
                    continue;

                neighbours.add(coordinates.add(i, j));
            }
        }
        return neighbours;
    }

    //returns the Color constant value of the tile given with the given offset applied
    //if the pixel doesn't exist, return TileType.Invalid
    private int getPixelColorWithOffset(Tile coordinates, int xDiff, int yDiff){
        int tileType = INVALID;

        try {
            tileType = mapImage.getPixel(coordinates.getX()+xDiff, coordinates.getY()+yDiff);

        } catch (IllegalArgumentException e){
            //pixel doesn't exist, ignore and move on
        }
        return tileType;
    }

    private Direction findRoomsAdjacentToDoor(Tile coordinatesOfDoor){

        //if horizontal direction has both sides free
        if (checkHorizontalDirection(coordinatesOfDoor))
            return VERTICAL;
            //if the vertical direction has both sides free
        else if (checkVerticalDirection(coordinatesOfDoor))
            return HORIZONTAL;

        //if neither vertical or horizontal have both sides free, then return invalid
        return Direction.INVALID;
    }

    //checks left and right of the door for if it is blocked by a room boundary or not
    //returns invalid if one or both sides are blocked
    private boolean checkHorizontalDirection(Tile coordinatesOfDoor){

        if (checkInDirection(coordinatesOfDoor, LEFT)){
            //if both are valid cells, return true, otherwise return false
            return checkInDirection(coordinatesOfDoor, RIGHT);
        }
        //one or both horizontal cells are invalid
        return false;
    }

    //checks above and below of the door for if it is blocked by a room boundary or not
    //returns false if one or both sides are blocked
    private boolean checkVerticalDirection(Tile coordinatesOfDoor){

        if (checkInDirection(coordinatesOfDoor, UP)){
            //if both are valid cells, return true, otherwise return false
            return checkInDirection(coordinatesOfDoor, DOWN);
        }
        //one or both vertical cells are invalid
        return false;
    }

    //checks the tile in the given direction for if it is a valid tile.
    //returns false if the direction is blocked by a room boundary
    private boolean checkInDirection(Tile coordinates, Direction direction){
        boolean validDirection = false;
        switch (direction){

            case UP:
                if (getPixelColorWithOffset(coordinates, 0, -1) != WALL)
                    validDirection = true;
                break;
            case DOWN:
                if (getPixelColorWithOffset(coordinates, 0, 1) != WALL)
                    validDirection = true;
                break;
            case LEFT:
                if (getPixelColorWithOffset(coordinates, -1, 0) != WALL)
                    validDirection = true;
                break;
            case RIGHT:
                if (getPixelColorWithOffset(coordinates, 1, 0) != WALL)
                    validDirection = true;
                break;
            default:
                break;
        }
        return validDirection;
    }

    private void initOpenSet(){
        openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
                return 0;
            return 1;
        });
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
