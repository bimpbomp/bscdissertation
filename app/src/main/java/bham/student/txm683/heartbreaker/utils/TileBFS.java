package bham.student.txm683.heartbreaker.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class TileBFS {

    private TileBFS(){

    }

    //gets the coordinates of the 8 surrounding cells. Warning: doesn't check if they're valid
    public static ArrayList<Tile> getNeighbours(Tile coordinates){
        ArrayList<Tile> neighbours = new ArrayList<>();

        for (int i = -1; i < 2; i++){
            for (int j = -1; j < 2; j++){
                if (i == 0 && j == 0)
                    continue;

                neighbours.add(coordinates.add(i, j));
            }
        }
        return neighbours;
    }

    public static PriorityQueue<Pair<Tile, Integer>> initOpenSet(){
        return new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
                return 0;
            return 1;
        });
    }

    /*private Tile searchForTileType(Tile startingPosition, int desiredTileType) throws MapConversionException {
        if (!TileType.isValidTileType(desiredTileType))
            throw new MapConversionException(MCEReason.SEARCH_FOR_INVALID_TILE_TYPE);

        //visited tiles are added here
        HashSet<Tile> closedSet = new HashSet<>();

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        PriorityQueue<Pair<Tile, Integer>> openSet = TileBFS.initOpenSet();
        openSet.add(new Pair<>(startingPosition, 0));

        //initialise variables needed in loop
        Pair<Tile, Integer> tileAndCost;
        Tile currentTile;
        int currentCost;
        int neighbourTileType;
        while (!openSet.isEmpty()) {

            //get the tile with the lowest cost
            tileAndCost = openSet.poll();
            currentTile = tileAndCost.first;
            currentCost = tileAndCost.second;

            //add it to the closed set so it isn't inspected again
            closedSet.add(currentTile);

            ArrayList<Tile> neighbours = TileBFS.getNeighbours(currentTile);

            for (Tile neighbour : neighbours) {
                //get the color of this neighbour
                neighbourTileType = getPixelColorWithOffset(neighbour,0,0);

                if (neighbourTileType == TileType.INVALID || closedSet.contains(neighbour)) {
                    //if the tile is out of bounds, or has already been inspected, move on
                    continue;
                } else if (neighbourTileType == desiredTileType) {
                    //if the tile is the goal, return
                    return neighbour;
                }

                //neighbour is a valid tile but not target, calc it's cost and add to openset
                int neighbourCost = currentCost + 1;
                openSet.add(new Pair<>(neighbour, neighbourCost));
            }
        }

        return startingPosition;
    }*/

}
