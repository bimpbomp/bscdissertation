package bham.student.txm683.heartbreaker.map;

import android.util.Pair;
import bham.student.txm683.heartbreaker.utils.Point;

public class Map {

    private String name;

    private int width, height;

    private Point[] enemySpawnLocations;
    private Point playerSpawnLocation;

    public Map(){
        initTestMap("TestMap");
    }

    public Map(String name){
        initTestMap(name);
    }

    private void initTestMap(String name){
        this.name = name;
        //init test map entity locations
        enemySpawnLocations = new Point[25];
        int count = 0;
        for (int i = 0; i < 5; i++){
            for (int j = 0; j < 5; j++) {
                enemySpawnLocations[count] = new Point(300+200*i,300+200*j);
                count++;
            }
        }
        playerSpawnLocation = new Point(100,100);
    }

    public void loadMap(int width, int height){
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
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

    public Point[] getEnemySpawnLocations() {
        return enemySpawnLocations;
    }

    public Point getPlayerSpawnLocation() {
        return playerSpawnLocation;
    }
}
