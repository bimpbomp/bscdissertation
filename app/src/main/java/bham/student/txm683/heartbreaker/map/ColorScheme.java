package bham.student.txm683.heartbreaker.map;

import android.graphics.Color;

import java.util.Random;

public class ColorScheme {
    public static final int DOOR_COLOR = Color.BLUE;
    public static final int WALL_COLOR = Color.rgb(32,32,32);
    public static final int CHASER_COLOR = Color.rgb(255, 153, 51);
    public static final int UPPER_PLAYER_COLOR = Color.WHITE;
    public static final int LOWER_PLAYER_COLOR = Color.MAGENTA;

    private static Random random = new Random();

    private ColorScheme(){

    }

    public static int randomColor(){
        return Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256));
    }
}