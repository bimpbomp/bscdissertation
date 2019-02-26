package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.*;

public class TileSet {
    private Map<Tile, List<Collidable>> permanents;
    private Map<Tile, List<Collidable>> temporaries;

    private int tileSize;

    private Set<Tile> tilesVisibleToPlayer;

    public TileSet(int tileSize){

        this.tileSize = tileSize;
        this.permanents = new HashMap<>();
        this.temporaries = new HashMap<>();

        this.tilesVisibleToPlayer = new HashSet<>();
    }

    public List<Collidable> getTileBin(Tile tile){
        List<Collidable> returnValue = new ArrayList<>();

        if (permanents.containsKey(tile))
            returnValue.addAll(permanents.get(tile));

        if (temporaries.containsKey(tile))
            returnValue.addAll(temporaries.get(tile));

        return returnValue;
    }

    public void clearVisibleSet(){
        this.tilesVisibleToPlayer.clear();
    }

    public Set<Tile> getTilesVisibleToPlayer() {
        return tilesVisibleToPlayer;
    }

    public boolean tileIsVisibleToPlayer(Tile tile){
        return this.tilesVisibleToPlayer.contains(tile);
    }

    public void addVisibleTile(Tile tile){
        this.tilesVisibleToPlayer.add(tile);
    }

    public List<Collidable> getViewBlockingObjectsAtTile(Tile tile){
        List<Collidable> solids = new ArrayList<>();
        if (permanents.containsKey(tile)){
            for (Collidable collidable : permanents.get(tile)){
                if (collidable instanceof Core || collidable instanceof Wall)
                    solids.add(collidable);
            }
        }

        if (temporaries.containsKey(tile)){
            for (Collidable collidable : temporaries.get(tile)){
                if (collidable instanceof Door)
                    solids.add(collidable);
            }
        }
        return solids;
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

    private void addToGrid(Map<Tile, List<Collidable>> map, Collidable collidable){
        Tile tile;

        addPointToGrid(map, collidable, Tile.mapToTile(collidable.getCenter(), tileSize));

        for (Point vertex : collidable.getCollisionVertices()){
            tile = Tile.mapToTile(vertex, tileSize);

            addPointToGrid(map, collidable, tile);
        }
    }

    private void addPointToGrid(Map<Tile, List<Collidable>> map, Collidable collidable, Tile tile){
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

    public int getTileSize() {
        return tileSize;
    }
}
