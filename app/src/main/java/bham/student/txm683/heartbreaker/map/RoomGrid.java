package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RoomGrid {
    private Tile[][] defaultTileSet;
    private int tileSize;
    private HashMap<Tile, Entity> entitiesInGrid;

    public RoomGrid(Tile[][] defaultTileSet, int tileSize){
        this.defaultTileSet = defaultTileSet;
        this.tileSize = tileSize;
        this.entitiesInGrid = new HashMap<>();
    }

    public void resetGrid(){
        this.entitiesInGrid.clear();
    }

    public void addEntity(Entity entity){
        Point[] vertices = entity.getShape().getCollisionVertices();

        //contains unique grid references
        Set<Tile> addedGridReferences = new HashSet<>();

        //adds entities vertices to grid
        for (Point point : vertices){

        }


        for (Tile gridReference : addedGridReferences){
            if (gridReference)
        }
    }
}
