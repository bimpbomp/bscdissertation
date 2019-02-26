package bham.student.txm683.heartbreaker.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Tile {

    private final int x;
    private final int y;

    public Tile(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Tile(Point point){
        this.x = (int) point.getX();
        this.y = (int) point.getY();
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

    public Tile add(int xToAdd, int yToAdd){
        return new Tile(x+xToAdd, y + yToAdd);
    }

    public Tile add(Tile t){
        return this.add(t.x, t.y);
    }

    public static Tile mapToTile(Point point, int tileSize){
        return new Tile(((int) point.getX()/tileSize) * tileSize, ((int) point.getY()/tileSize) * tileSize);
    }

    public static Tile mapToCenterOfTile(Point point, int tileSize){
        return mapToTile(point, tileSize).add(tileSize/2, tileSize/2);
    }

    public static Tile mapToCenterOfTile(Tile tile, int tileSize){
        return mapToTile(new Point(tile), tileSize).add(tileSize/2, tileSize/2);
    }



    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Tile))
            return false;

        return (x == ((Tile) obj).x) && (y == ((Tile) obj).y);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 41).append(x).append(y).toHashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return x+","+y;
    }
}
