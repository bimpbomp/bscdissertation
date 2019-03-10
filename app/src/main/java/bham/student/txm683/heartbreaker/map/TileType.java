package bham.student.txm683.heartbreaker.map;


import android.graphics.Color;

import java.util.HashSet;

public class TileType {
    static final int WALL = Color.BLACK;
    static final int PLAYER = Color.WHITE;
    static final int DRONE = Color.YELLOW;
    static final int DOOR = Color.BLUE;
    static final int INVALID = Color.GRAY;
    static final int CORE = Color.GREEN;
    static final int TURRET = Color.RED;

    private static HashSet<Integer> colorConstants = new HashSet<>();

    static {

        colorConstants.add(WALL);
        colorConstants.add(PLAYER);
        colorConstants.add(DRONE);
        colorConstants.add(DOOR);
        colorConstants.add(INVALID);
        colorConstants.add(CORE);

    }

    private TileType(){

    }

    static boolean isValidTileType(int color){
        return colorConstants.contains(color);
    }
}
