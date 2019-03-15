package bham.student.txm683.heartbreaker.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.exceptions.MCEReason;
import bham.student.txm683.heartbreaker.utils.exceptions.MapConversionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static bham.student.txm683.heartbreaker.map.MapReader.Direction.*;
import static bham.student.txm683.heartbreaker.map.TileType.DOOR;
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

    private Context context;
    private Bitmap mapImage;

    private List<List<Integer>> meshGenList;
    private List<DoorBuilder> doorSpawns;

    private int tileSize;
    private Point centerOffset;

    private int width, height;

    public MapReader(Context context){
        this.context = context;
        this.meshGenList = new ArrayList<>();
        this.doorSpawns = new ArrayList<>();

        width = 0;
        height = 0;
    }

    public List<List<Integer>> getMeshGenList() {
        return meshGenList;
    }

    public List<DoorBuilder> getDoorSpawns(){
        return doorSpawns;
    }

    public void loadMap(String mapName, String stage, int tileSize, List<DoorBuilder> doorBuilders) throws MapConversionException {
        this.tileSize = tileSize;
        this.centerOffset = new Point(tileSize/2f, tileSize/2f);

        this.doorSpawns = doorBuilders;

        convertMapBitmapToGraph(mapName, stage);
    }

    private Bitmap openImage(String mapName, String stage) throws MapConversionException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(String.format(Locale.UK,"maps/%s/%s_%s.png", mapName, mapName, stage)));
        } catch (IOException e){
            throw new MapConversionException(MCEReason.FILE_DECODING, e);
        }
    }

    private void convertMapBitmapToGraph(String mapName, String stage) throws MapConversionException {

        mapImage = openImage(mapName, stage);

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
                    if (TileType.isDoor(pixel)){

                        Tile tile = new Tile(columnIdx, rowIdx);

                        modifyDoorBuilder(tile);

                        tileValues.add(-2);

                    } else {
                        tileValues.add(0);
                    }
                }
            }

            meshGenList.add(tileValues);
        }
    }

    private Point convertToGlobal(Tile tile){
        return new Point(tile).sMult(tileSize).add(centerOffset);
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

    private void modifyDoorBuilder(Tile coordinatesOfDoor){
        boolean vertical;

        //if horizontal direction has both sides free
        if (checkHorizontalDirection(coordinatesOfDoor, WALL) && checkHorizontalDirection(coordinatesOfDoor, DOOR))
            vertical = true;
            //if the vertical direction has both sides free
        else if (checkVerticalDirection(coordinatesOfDoor, WALL) && checkVerticalDirection(coordinatesOfDoor, DOOR))
            vertical = false;
        else
            return;

        for (DoorBuilder doorBuilder : doorSpawns){
            if (doorBuilder.getLiesOn().equals(coordinatesOfDoor)){
                doorBuilder.setVertical(vertical);
                doorBuilder.setCenter(convertToGlobal(coordinatesOfDoor));
            }
        }
    }

    //checks left and right of the door for if it is blocked by a room boundary or not
    //returns invalid if one or both sides are blocked
    private boolean checkHorizontalDirection(Tile coordinatesOfDoor, int checkingFor){

        if (checkDirectionDoesntHaveTileType(coordinatesOfDoor, LEFT, checkingFor)){
            //if both are valid cells, return true, otherwise return false
            return checkDirectionDoesntHaveTileType(coordinatesOfDoor, RIGHT, checkingFor);
        }
        //one or both horizontal cells are invalid
        return false;
    }

    //checks above and below of the door for if it is blocked by a room boundary or not
    //returns false if one or both sides are blocked
    private boolean checkVerticalDirection(Tile coordinatesOfDoor, int checkingFor){

        if (checkDirectionDoesntHaveTileType(coordinatesOfDoor, UP, checkingFor)){
            //if both are valid cells, return true, otherwise return false
            return checkDirectionDoesntHaveTileType(coordinatesOfDoor, DOWN, checkingFor);
        }
        //one or both vertical cells are invalid
        return false;
    }

    //checks the tile in the given direction for if it is a valid tile.
    //returns false if the direction is blocked by a room boundary or door
    private boolean checkDirectionDoesntHaveTileType(Tile coordinates, Direction direction, int checkingFor){
        boolean validDirection = false;
        switch (direction){

            case UP:
                if (getPixelColorWithOffset(coordinates, 0, -1) != checkingFor)
                    validDirection = true;
                break;
            case DOWN:
                if (getPixelColorWithOffset(coordinates, 0, 1) != checkingFor)
                    validDirection = true;
                break;
            case LEFT:
                if (getPixelColorWithOffset(coordinates, -1, 0) != checkingFor)
                    validDirection = true;
                break;
            case RIGHT:
                if (getPixelColorWithOffset(coordinates, 1, 0) != checkingFor)
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
