package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Overlord;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.HashMap;
import java.util.List;

public class Map implements IMap {

    private String name;
    private String stage;

    private int tileSize, widthInTiles, heightInTiles;

    private Player player;

    private List<AIEntity> enemies;
    private List<Wall> walls;

    private java.util.Map<String, Door> doors;

    private List<Pickup> pickups;

    private Graph<Integer> meshGraph;
    private HashMap<Integer, MeshPolygon> rootMeshPolygons;

    private List<Overlord> overlords;

    @SuppressLint("UseSparseArrays")
    public Map(String name, String stage, int tileSize){
        this.name = name;
        this.stage = stage;

        this.tileSize = tileSize;
        this.doors = new HashMap<>();

        this.rootMeshPolygons = new HashMap<>();

    }

    public List<Overlord> getOverlords() {
        return overlords;
    }

    public void setOverlords(List<Overlord> overlords) {
        this.overlords = overlords;
    }

    @Override
    public String getStage() {
        return stage;
    }

    @Override
    public Graph<Integer> getMeshGraph() {
        return meshGraph;
    }

    @Override
    public void setMeshGraph(Graph<Integer> meshGraph) {
        this.meshGraph = meshGraph;
    }

    @Override
    public void setRootMeshPolygons(List<MeshPolygon> meshSets){
        for (MeshPolygon meshSet : meshSets){
            rootMeshPolygons.put(meshSet.getId(), meshSet);
        }
    }

    @Override
    public java.util.Map<Integer, MeshPolygon> getRootMeshPolygons() {
        return rootMeshPolygons;
    }

    public List<Pickup> getPickups() {
        return pickups;
    }

    public void setPickups(List<Pickup> pickups) {
        this.pickups = pickups;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setEnemies(List<AIEntity> enemies) {
        this.enemies = enemies;
    }

    @Override
    public void setDoors(List<Door> doors) {
        for (Door door : doors){
            this.doors.put(door.getName(), door);
        }
    }

    public List<AIEntity> getEnemies() {
        return enemies;
    }

    public java.util.Map<String, Door> getDoors() {
        return doors;
    }

    @Override
    public float getHeight(){
        return heightInTiles * tileSize;
    }

    @Override
    public float getWidth(){
        return widthInTiles * tileSize;
    }

    @Override
    public void setWidthInTiles(int widthInTiles) {
        this.widthInTiles = widthInTiles;
    }

    @Override
    public void setHeightInTiles(int heightInTiles) {
        this.heightInTiles = heightInTiles;
    }

    @Override
    public List<Wall> getWalls() {
        return walls;
    }

    @Override
    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }
}
