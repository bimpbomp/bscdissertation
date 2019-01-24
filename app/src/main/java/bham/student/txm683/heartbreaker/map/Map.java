package bham.student.txm683.heartbreaker.map;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.ArrayList;

public class Map {

    private String name;

    private int width, height;

    private ArrayList<Pair<Integer, Point>> enemySpawnLocations;
    private Point playerSpawnLocation;
    private ArrayList<Point> staticSpawns;

    private int tileSize;

    public Map(){
        enemySpawnLocations = new ArrayList<>();
        playerSpawnLocation = new Point();
        staticSpawns = new ArrayList<>();
        this.tileSize = 0;
    }

    private void initTestMap(String name){
        this.name = name;

        //simulating file reading
        int[][] mapTiles = new int[][]{
                {1,1,1,1,1,1,1,1,1},
                {1,0,0,1,0,0,0,0,1},
                {1,0,0,1,3,0,4,3,1},
                {1,0,1,1,1,0,1,1,1},
                {1,0,3,0,2,0,0,3,1},
                {1,0,0,1,0,0,1,0,1},
                {1,1,1,1,1,1,1,1,1},};

        for (int row = 0; row < mapTiles.length; row++){
            for (int column = 0; column < mapTiles[row].length; column++){
                Point spawnTile = new Point(column* tileSize, row* tileSize);
                Point centerOffset = new Point(tileSize /2f, tileSize /2f);
                Point spawnLocation = spawnTile.add(centerOffset);

                //Log.d("hb::Map", "spawn: " + spawnLocation.toString());
                switch (mapTiles[row][column]){
                    case (0):
                        break;
                    case (1):
                        Log.d("hb::Map", "static: " + spawnLocation);
                        staticSpawns.add(spawnLocation);
                        break;
                    case (2):
                        Log.d("hb::Map", "player: " + spawnLocation);
                        playerSpawnLocation = spawnLocation;
                        break;
                    case (3):
                        Log.d("hb::Map", "enemy triangle: " + spawnLocation);
                        enemySpawnLocations.add(new Pair<>(3, spawnLocation));
                        break;
                    case (4):
                        Log.d("hb::Map", "enemy rectangle: " + spawnLocation);
                        enemySpawnLocations.add(new Pair<>(4, spawnLocation));
                        break;
                    case (5):
                        Log.d("hb::Map", "enemy circle: " + spawnLocation);
                        enemySpawnLocations.add(new Pair<>(5, spawnLocation));
                    default:
                        Log.d("hb::Map", "Invalid");
                }
            }
            this.width = mapTiles[0].length*tileSize;
            this.height = mapTiles.length*tileSize;
        }
    }

    public void loadMap(String mapName, int tileSize){
        this.tileSize = tileSize;

        initTestMap(mapName);
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

    public Pair[] getEnemySpawnLocations() {
        return enemySpawnLocations.toArray(new Pair[0]);
    }

    public Point[] getStaticSpawns(){
        return staticSpawns.toArray(new Point[0]);
    }

    public Point getPlayerSpawnLocation() {
        return playerSpawnLocation;
    }

    public int getTileSize() {
        return tileSize;
    }
}
