package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Portal;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map {

    private String name;
    private String stage;

    private int tileSize, widthInTiles, heightInTiles;

    private Player player;

    private List<AIEntity> enemies;
    private List<Wall> walls;

    private HashMap<String, Door> doors;

    private List<Perimeter> perimeters;

    private List<Pickup> pickups;

    private Portal portal;

    private Graph<Integer> meshGraph;
    private HashMap<Integer, MeshPolygon> rootMeshPolygons;


    @SuppressLint("UseSparseArrays")
    public Map(String name, String stage, int tileSize){
        this.name = name;
        this.stage = stage;

        this.tileSize = tileSize;
        this.doors = new HashMap<>();

        this.rootMeshPolygons = new HashMap<>();

        this.perimeters = new ArrayList<>();

    }

    public String getStage() {
        return stage;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public Graph<Integer> getMeshGraph() {
        return meshGraph;
    }

    public void setMeshGraph(Graph<Integer> meshGraph) {
        this.meshGraph = meshGraph;
    }

    public void setRootMeshPolygons(List<MeshPolygon> meshSets){
        for (MeshPolygon meshSet : meshSets){
            rootMeshPolygons.put(meshSet.getId(), meshSet);
        }
    }

    public HashMap<Integer, MeshPolygon> getRootMeshPolygons() {
        return rootMeshPolygons;
    }

    public List<Pickup> getPickups() {
        return pickups;
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

    public void setEnemies(List<AIEntity> enemies) {
        this.enemies = enemies;
    }

    public void setDoors(List<Door> doors) {
        for (Door door : doors){
            this.doors.put(door.getName(), door);
        }
    }

    public List<AIEntity> getEnemies() {
        return enemies;
    }

    public List<Perimeter> getRoomPerimeters() {
        return perimeters;
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

    public List<Wall> getWalls() {
        return walls;
    }

    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }
}
