package bham.student.txm683.heartbreaker.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Tile {

    private final int x;
    private final int y;

    public Tile(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Tile(String tileString){
        String[] split = tileString.split(",");
        this.x = Integer.parseInt(split[0]);
        this.y = Integer.parseInt(split[1]);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Tile))
            return false;

        return (x == ((Tile) obj).x) && (y == ((Tile) obj).y);
    }

    @NonNull
    @Override
    public String toString() {
        return x+","+y;
    }
}
