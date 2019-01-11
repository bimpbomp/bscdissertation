package bham.student.txm683.heartbreaker.map;

import android.util.Pair;

public class Map {

    private int width, height;

    public Map(){

    }

    public void loadMap(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Pair<Integer, Integer> getDimensions(){
        return new Pair<>(width, height);
    }
}
