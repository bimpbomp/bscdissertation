package bham.student.txm683.heartbreaker.map;

import android.graphics.Color;

public enum TileType {
    ROOM_BOUNDARY (Color.BLACK),
    PLAYER (Color.CYAN),
    CHASER (Color.rgb(255, 153, 51)),
    DOOR (Color.BLUE),
    INVALID (Color.GRAY);

    private int colorValue;

    TileType(int colorValue){
        this.colorValue = colorValue;
    }

    public int getColorValue(){
        return this.colorValue;
    }
}

