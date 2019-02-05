package bham.student.txm683.heartbreaker.map;

import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.EnemyType;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomGraph;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.HashMap;
import java.util.List;

public class Map {
    private static final String TAG = "hb::Map";

    private String name;

    private int tileSize, widthInTiles, heightInTiles;

    private Point playerSpawn;

    private List<Point> staticEntities;
    private List<Pair<EnemyType, Point>> enemies;

    private HashMap<Integer, Perimeter> roomPerimeters;

    private RoomGraph roomGraph;
    private List<Door> doors;

    public Map(String name, int tileSize){
        this.name = name;
        this.tileSize = tileSize;
    }

    public Point mapTileToGlobalPoint(Tile tile){
        Point point = new Point(tile.getX(), tile.getY()).smult(tileSize);
        return new Point(point.getX()+tileSize/2f, point.getY()+tileSize/2f);
    }

    public Tile mapGlobalPointToTile(Point globalCoordinate){
        int x = (int) Math.floor(globalCoordinate.getX()/tileSize);
        int y = (int) Math.floor(globalCoordinate.getY()/tileSize);
        return new Tile(x,y);
    }

    public Point mapGlobalPointToTilePoint(Point globalCoordinate){
        int x = (int) Math.floor(globalCoordinate.getX()/tileSize);
        int y = (int) Math.floor(globalCoordinate.getY()/tileSize);
        return new Point(x,y);
    }

    public String getName() {
        return name;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setPlayerSpawn(Point playerSpawn) {
        this.playerSpawn = playerSpawn;
    }

    public void setStaticEntities(List<Point> staticEntities) {
        this.staticEntities = staticEntities;
    }

    public void setEnemies(List<Pair<EnemyType, Point>> enemies) {
        this.enemies = enemies;
    }

    public void setRoomPerimeters(HashMap<Integer, Perimeter> roomPerimeters) {
        this.roomPerimeters = roomPerimeters;
    }

    public void setRoomGraph(RoomGraph roomGraph) {
        this.roomGraph = roomGraph;
    }

    public void setDoors(List<Door> doors) {
        this.doors = doors;
    }

    public Point getPlayerSpawn() {
        return playerSpawn;
    }

    public List<Point> getStaticEntities() {
        return staticEntities;
    }

    public List<Pair<EnemyType, Point>> getEnemies() {
        return enemies;
    }

    public HashMap<Integer, Perimeter> getRoomPerimeters() {
        return roomPerimeters;
    }

    public RoomGraph getRoomGraph() {
        return roomGraph;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public float getHeight(){
        return heightInTiles * tileSize;
    }

    public float getWidth(){
        return widthInTiles * tileSize;
    }

    public void setWidthInTiles(int widthInTiles) {
        this.widthInTiles = widthInTiles;
    }

    public void setHeightInTiles(int heightInTiles) {
        this.heightInTiles = heightInTiles;
    }
}
