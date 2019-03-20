package bham.student.txm683.heartbreaker.map;

import android.graphics.Color;

import java.util.Random;

public class ColorScheme {
    public static final int DOOR_COLOR = Color.BLUE;
    public static final int WALL_COLOR = Color.rgb(32,32,32);
    public static final int CHASER_COLOR = Color.rgb(255, 153, 51);
    public static final int PLAYER_COLOR = Color.WHITE;
    public static final int LOWER_PLAYER_COLOR = Color.MAGENTA;

    private static Random random = new Random();

    private ColorScheme(){

    }

    public static int randomColor(){
        return Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256));
    }

    public static int randomGreen(){
        return Color.rgb(0, 77 + random.nextInt(79), 0);
    }

    public static int randomGrey(){
        int colorV = random.nextInt(256);
        return Color.rgb(colorV, colorV, colorV);
    }

    //Credit: Gary McGowan: https://stackoverflow.com/questions/33072365/how-to-darken-a-given-color-int
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
}