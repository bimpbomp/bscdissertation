package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RoomGrid {
    //tiles in the grid, relative to global tile grid so not guaranteed to start at 0,0
    private HashSet<Tile> tileSet;
    private int tileSize;

    //entities that wont move or be destroyed
    private HashMap<Tile, HashSet<Entity>> permanentEntities;
    //each tile has a bucket for unique entities
    private HashMap<Tile, HashSet<Entity>> entitiesInGrid;

    public RoomGrid(List<Tile> tileSet, int tileSize){
        this.tileSet = new HashSet<>(tileSet);
        this.tileSize = tileSize;

        this.permanentEntities = new HashMap<>();

        this.entitiesInGrid = new HashMap<>();
    }

    public void resetGrid(){
        this.entitiesInGrid.clear();
    }

    public void addEntity(Entity entity){
        Point[] vertices = entity.getShape().getCollisionVertices();

        //adds entities vertices to tiles that it's vertices exist in
        for (Point point : vertices){
            Tile mappedPoint = mapGlobalPointToTile(point);
            if (tileSet.contains(mappedPoint)){
                if (entitiesInGrid.get(mappedPoint) != null)
                    entitiesInGrid.get(mappedPoint).add(entity);
                else {
                    HashSet<Entity> set = new HashSet<>();
                    set.add(entity);
                    entitiesInGrid.put(mappedPoint, set);
                }
            }
        }
    }

    public Tile mapGlobalPointToTile(Point globalCoordinate){
        return new Tile((int) Math.floor(globalCoordinate.getX()/tileSize),
                (int) Math.floor(globalCoordinate.getY()/tileSize));
    }
}
