package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.*;

public class TileSet {
    private HashMap<Tile, List<Collidable>> permanents;
    private HashMap<Tile, List<Collidable>> temporaries;

    public TileSet(){
        this.permanents = new HashMap<>();
        this.temporaries = new HashMap<>();
    }

    public void addPermanentToGrid(Collidable collidable){
        addToGrid(permanents, collidable);
    }

    public void clearTemporaries(){
        this.temporaries.clear();
    }

    public void addTemporaryToGrid(Collidable collidable){
        addToGrid(temporaries, collidable);
    }

    public Set<Collidable> getCollidablesAt(Tile tile){
        Set<Collidable> returnList = new HashSet<>();

        if (permanents.containsKey(tile) && permanents.get(tile) != null)
            returnList.addAll(permanents.get(tile));

        if (temporaries.containsKey(tile) && temporaries.get(tile) != null)
            returnList.addAll(temporaries.get(tile));

        return returnList;
    }

    private void addToGrid(HashMap<Tile, List<Collidable>> map, Collidable collidable){
        Tile tile;
        for (Point vertex : collidable.getCollisionVertices()){
            tile = new Tile(vertex);

            if (map.containsKey(tile)){
                //if the tile has a mapping

                if (map.get(tile) != null)
                    //if list already exists, add collidable to it
                    map.get(tile).add(collidable);
                else {
                    //otherwise first create list then add collidable
                    map.put(tile, new ArrayList<>());
                    map.get(tile).add(collidable);
                }
            } else {
                //tile is not in map yet, add entry and add collidable
                map.put(tile, new ArrayList<>());
                map.get(tile).add(collidable);
            }
        }
    }
}
