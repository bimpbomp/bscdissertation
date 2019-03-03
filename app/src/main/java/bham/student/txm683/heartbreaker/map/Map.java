package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.TileSet;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomGraph;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map {
    private static final String TAG = "hb::Map";

    private String name;

    private int tileSize, widthInTiles, heightInTiles;

    private Player player;

    private ArrayList<AIEntity> enemies;
    private List<Wall> walls;

    private HashMap<Integer, Room> rooms;
    private HashMap<String, Door> doors;

    private List<Pickup> pickups;

    private TileSet tileSet;

    private Core core;

    private RoomGraph roomGraph;

    private HashMap<Integer, RoomGrid> roomGrids;

    private Graph<Integer> meshGraph;
    private HashMap<Integer, MeshSet> rootMeshSets;


    public Map(String name, int tileSize){
        this.name = name;
        this.tileSize = tileSize;
        this.doors = new HashMap<>();

        this.rootMeshSets = new HashMap<>();
    }

    public Graph<Integer> getMeshGraph() {
        return meshGraph;
    }

    public void setMeshGraph(Graph<Integer> meshGraph) {
        this.meshGraph = meshGraph;
    }

    public void setRootMeshSets(List<MeshSet> meshSets){
        for (MeshSet meshSet : meshSets){
            rootMeshSets.put(meshSet.getId(), meshSet);
        }
    }

    public HashMap<Integer, MeshSet> getRootMeshSets() {
        return rootMeshSets;
    }

    public TileSet getTileSet() {
        return tileSet;
    }

    public void setTileSet(TileSet tileSet) {
        this.tileSet = tileSet;
    }

    public Point mapTileToGlobalPoint(Tile tile){
        Point point = new Point(tile.getX(), tile.getY()).sMult(tileSize);
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

    public List<Pickup> getPickups() {
        return pickups;
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void setPickups(List<Pickup> pickups) {
        this.pickups = pickups;
    }

    public String getName() {
        return name;
    }

    public int getTileSize() {
        return tileSize;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setEnemies(ArrayList<AIEntity> enemies) {
        this.enemies = enemies;
    }

    public void setRooms(HashMap<Integer, Room> rooms) {
        this.rooms = rooms;
    }

    public void setRoomGraph(RoomGraph roomGraph) {
        this.roomGraph = roomGraph;
    }

    public void setDoors(List<Door> doors) {
        for (Door door : doors){
            this.doors.put(door.getName(), door);
        }
    }

    public ArrayList<AIEntity> getEnemies() {
        return enemies;
    }

    public HashMap<Integer, Room> getRoomPerimeters() {
        return rooms;
    }

    public RoomGraph getRoomGraph() {
        return roomGraph;
    }

    public HashMap<String, Door> getDoors() {
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

    public HashMap<Integer, Room> getRooms() {
        return rooms;
    }

    public HashMap<Integer, RoomGrid> getRoomGrids() {
        return roomGrids;
    }

    public void setRoomGrids(HashMap<Integer, RoomGrid> roomGrids) {
        this.roomGrids = roomGrids;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }
}
