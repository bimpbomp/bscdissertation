package bham.student.txm683.framework.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

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

    public Tile(JSONObject jsonObject) throws JSONException {
        this.x = jsonObject.getInt("x");
        this.y = jsonObject.getInt("y");
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
