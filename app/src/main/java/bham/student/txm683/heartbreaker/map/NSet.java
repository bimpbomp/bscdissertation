package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.ArrayList;
import java.util.List;

public class NSet {
    private int id;
    private List<Tile> containedTiles;

    public NSet(int id, Tile initialTile){
        this.id = id;
        this.containedTiles = new ArrayList<>();
        this.containedTiles.add(initialTile);
    }

    public boolean contains(Tile tile){
        return containedTiles.contains(tile);
    }

    public int getId(){
        return this.id;
    }

    public List<Tile> getContainedTiles() {
        return containedTiles;
    }

    public void add(Tile tile){
        containedTiles.add(tile);
    }
}
