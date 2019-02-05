package bham.student.txm683.heartbreaker.utils;

import java.util.ArrayList;

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

}
