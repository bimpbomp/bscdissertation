package bham.student.txm683.heartbreaker.map;


import android.graphics.Color;

public class TileType {
    static final int WALL = Color.BLACK;
    static final int DOOR = Color.BLUE;

    static final int PLAYER = Color.WHITE;
    static final int DRONE = Color.YELLOW;
    static final int CORE = Color.GREEN;
    static final int TURRET = Color.RED;

    static final int HEALTH = Color.BLUE;
    static final int BOMB = Color.WHITE;

    private TileType(){

    }

    static boolean isEntity(int color){
        return color == PLAYER || color == TURRET || color == DRONE || color == CORE;
    }

    static boolean isKey(int color){
        return isLockedDoor(color);
    }

    static boolean isLockedDoor(int color){
        return (Color.blue(color) == 0 && Color.green(color) == 0) && Color.red(color) != 0;
    }

    static boolean isDoor(int color){
        return color == DOOR || isLockedDoor(color);
    }

    static boolean isPickup(int color){
        return color == HEALTH || color == BOMB;
    }
}
