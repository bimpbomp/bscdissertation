package bham.student.txm683.heartbreaker.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Pair;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.exceptions.MCEReason;
import bham.student.txm683.heartbreaker.utils.exceptions.MapConversionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static bham.student.txm683.heartbreaker.map.MapReader.Direction.*;
import static bham.student.txm683.heartbreaker.map.TileType.WALL;

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

    private List<List<Integer>> meshGenList;
    private List<Pair<Integer, Point>> spawnLocations;
    private List<DoorBuilder> doorSpawns;
    private List<KeyBuilder> keyBuilders;
    private List<Pair<Integer, Point>> pickupLocations;

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

        this.keyBuilders = new ArrayList<>();
        this.pickupLocations = new ArrayList<>();
    }

    public List<List<Integer>> getMeshGenList() {
        return meshGenList;
    }

    public List<DoorBuilder> getDoorSpawns(){
        return doorSpawns;
    }

    public List<Pair<Integer, Point>> getSpawnLocations() {
        return spawnLocations;
    }

    public List<KeyBuilder> getKeyBuilders() {
        return keyBuilders;
    }

    public List<Pair<Integer, Point>> getPickupLocations() {
        return pickupLocations;
    }

    public void loadMap(String mapName, int tileSize) throws MapConversionException {
        this.tileSize = tileSize;
        this.centerOffset = new Point(tileSize/2f, tileSize/2f);

        convertMapBitmapToGraph(mapName);
        readInEntities(mapName);
        readInPickups(mapName);
    }

    private Bitmap openImage(String mapName, int frameNo) throws MapConversionException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(String.format(Locale.UK,"maps/%s/%s_%d.png", mapName, mapName, frameNo)));
        } catch (IOException e){
            throw new MapConversionException(MCEReason.FILE_DECODING, e);
        }
    }

    private void readInEntities(String mapName) throws MapConversionException {

        mapImage = openImage(mapName, 1);

        //dimensions in pixels/tiles
        height = mapImage.getHeight();
        width = mapImage.getWidth();

        for (int rowIdx = 0; rowIdx < height; rowIdx++){

            for (int columnIdx = 0; columnIdx < width; columnIdx++){

                int pixel = mapImage.getPixel(columnIdx, rowIdx);

                if (TileType.isEntity(pixel)){
                    spawnLocations.add(new Pair<>(pixel, convertToGlobal(columnIdx, rowIdx)));
                }
            }
        }
    }

    private void readInPickups(String mapName) throws MapConversionException {

        mapImage = openImage(mapName, 2);

        //dimensions in pixels/tiles
        height = mapImage.getHeight();
        width = mapImage.getWidth();

        for (int rowIdx = 0; rowIdx < height; rowIdx++){

            for (int columnIdx = 0; columnIdx < width; columnIdx++){

                int pixel = mapImage.getPixel(columnIdx, rowIdx);

                if (TileType.isKey(pixel)){
                    keyBuilders.add(new KeyBuilder(pixel, convertToGlobal(columnIdx, rowIdx)));
                } else if (TileType.isPickup(pixel)) {
                    pickupLocations.add(new Pair<>(pixel, convertToGlobal(columnIdx, rowIdx)));
                }
            }
        }
    }

    private void convertMapBitmapToGraph(String mapName) throws MapConversionException {

        mapImage = openImage(mapName, 0);

        //dimensions in pixels/tiles
        height = mapImage.getHeight();
        width = mapImage.getWidth();

        List<Integer> tileValues;
        for (int rowIdx = 0; rowIdx < height; rowIdx++){
            tileValues = new ArrayList<>();

            for (int columnIdx = 0; columnIdx < width; columnIdx++){

                int pixel = mapImage.getPixel(columnIdx, rowIdx);

                if (pixel == WALL){
                    tileValues.add(-1);

                } else {
                    Color color = Color.valueOf(pixel);

                    if (TileType.isDoor(pixel)){

                        boolean locked = (color.blue() == 0 && color.green() == 0);

                        Direction d = findRoomsAdjacentToDoor(new Tile(columnIdx, rowIdx));

                        doorSpawns.add(new DoorBuilder(locked, d==VERTICAL, convertToGlobal(columnIdx, rowIdx), pixel));

                        tileValues.add(-2);

                    } else {
                        tileValues.add(0);
                    }
                }
            }

            meshGenList.add(tileValues);
        }
    }

    /*private void convertMapBitmapToGraph(String mapName) throws MapConversionException {

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        try {
            //mapImage = BitmapFactory.decodeResource(context.getResources(), R.raw.map1, o);

            //mapImage = BitmapFactory.decodeStream(context.getAssets().open(String.format("maps/%s/%s_0.png", mapName, mapName)));
            mapImage = BitmapFactory.decodeStream(context.getAssets().open(String.format("maps/%s.png", mapName)));
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

                    } else if (pixel == DOOR){

                        Direction d = findRoomsAdjacentToDoor(new Tile(columnIdx, rowIdx));
                        Log.d("DOORSPAWN", columnIdx + ", " + rowIdx + ": " + d.name());

                        doorSpawns.add(new Pair<>(convertToGlobal(columnIdx, rowIdx), (d == VERTICAL)));

                        tileValues.add(-2);

                    } else {
                        spawnLocations.add(new Pair<>(pixel, convertToGlobal(columnIdx, rowIdx)));
                        tileValues.add(0);
                    }

                } else {
                    Log.d("MAP_READER", "Invalid tile type at: (" + columnIdx + "," + rowIdx + ")");
                    tileValues.add(0);


                }
            }

            meshGenList.add(tileValues);
        }
    }*/

    private Point convertToGlobal(int columnIdx, int rowIdx){
        return new Point(columnIdx, rowIdx).sMult(tileSize).add(centerOffset);
    }

    //returns the Color constant value of the tile given with the given offset applied
    //if the pixel doesn't exist, return TileType.Invalid
    private int getPixelColorWithOffset(Tile coordinates, int xDiff, int yDiff){
        int tileType = 0;

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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
