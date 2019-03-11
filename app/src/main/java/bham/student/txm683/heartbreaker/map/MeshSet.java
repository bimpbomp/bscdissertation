package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.ArrayList;
import java.util.List;

class MeshSet {
    private int id;
    private List<Tile> containedTiles;

    private Tile startTile;
    private int distanceToWall;

    MeshSet(int id, Tile initialTile, int distanceToWall){
        this.id = id;
        this.distanceToWall = distanceToWall;

        this.startTile = initialTile;

        this.containedTiles = new ArrayList<>();
        this.containedTiles.add(initialTile);
    }

    MeshSet(int id, List<Tile> containedTiles){
        this.id = id;
        this.containedTiles = containedTiles;

        this.startTile = new Tile(0,0);
        this.distanceToWall = 0;
    }

    public boolean hasTile(Tile tile){
        return containedTiles.contains(tile);
    }

    public int getId() {
        return id;
    }

    public Tile getStartTile(){
        return startTile;
    }

    public int getDistanceToWall() {
        return distanceToWall;
    }

    public void removeTile(Tile tile){
        containedTiles.remove(tile);
    }

    public List<Tile> getContainedTiles() {
        return containedTiles;
    }

    public void add(Tile tile){
        if (!containedTiles.contains(tile))
            containedTiles.add(tile);
    }

    public List<Tile> intersection(MeshSet b){
        List<Tile> intersectionSet = new ArrayList<>();

        for (Tile tile : containedTiles){
            if (b.containedTiles.contains(tile))
                intersectionSet.add(tile);
        }
        return intersectionSet;
    }
}
