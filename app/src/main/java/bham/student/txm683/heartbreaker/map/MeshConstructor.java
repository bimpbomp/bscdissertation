package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.util.Log;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.*;

public class MeshConstructor {

    private HashMap<Integer, NSet> existingSets;
    private Graph<Integer> meshGraph;
    private Queue<Tile> frontier;
    private List<List<Integer>> tileList;

    private UniqueID uniqueID;

    @SuppressLint("UseSparseArrays")
    public MeshConstructor(){
        existingSets = new HashMap<>();
        meshGraph = new Graph<>();
        frontier = new LinkedList<>();

        uniqueID = new UniqueID(1);
    }

    public HashMap<Integer, NSet> getExistingSets() {
        return existingSets;
    }

    public Graph<Integer> getMeshGraph() {
        return meshGraph;
    }

    public void constructMesh(List<List<Integer>> tileList){
        this.tileList = tileList;

        frontier.add(findStartingTile());

        Tile currentTile;
        List<Tile> surround;
        List<List<Tile>> squares;
        List<Tile> visitedNeighbours;
        while (!frontier.isEmpty()){

            currentTile = frontier.poll();
            System.out.println("Current tile: " + currentTile);

            surround = form3x3(currentTile);

            squares = form2x2s(surround);

            visitedNeighbours = getNeighbours(currentTile);

            if (visitedNeighbours.size() == 0){
                createNSet(currentTile, null);
            } else {
                boolean cellDealtWith = false;

                for (Tile visitedNeighbour : visitedNeighbours){
                    List<List<Tile>> squaresWN = getSquaresWithNeighbour(visitedNeighbour, squares);

                    boolean addToNeighbour = true;

                    for (List<Tile> square : squaresWN){

                        if (!tilesCanBeInSameSet(square)){
                            addToNeighbour = false;
                            break;
                        }
                    }

                    if (addToNeighbour){
                        addTileToNSet(currentTile, visitedNeighbour);
                        cellDealtWith = true;
                        break;
                    }
                }

                if (!cellDealtWith){
                    createNSet(currentTile, visitedNeighbours);
                }

                makeConnections(currentTile, visitedNeighbours);
            }

            addEmptyNeighboursToFrontier(surround);

            //printTileList();
        }
    }

    public void printTileList(){
        System.out.println();
        for (List<Integer> row : tileList){
            System.out.println();

            for (int cell : row){
                System.out.print(cell + ", ");
            }

            System.out.println();
        }
        System.out.println();
    }

    public String tileListToString(){
        StringBuilder stringBuilder = new StringBuilder();

        for (List<Integer> row : tileList){
            stringBuilder.append("\n");

            for (int cell : row){
                stringBuilder.append(cell);
                stringBuilder.append(", ");
            }

            stringBuilder.append("END \n");
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    private Tile findStartingTile(){
        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){
            for (int columnIdx = 0; columnIdx < tileList.get(rowIdx).size(); columnIdx++){
                if (tileList.get(rowIdx).get(columnIdx) == 0){
                    return new Tile(columnIdx, rowIdx);
                }
            }
        }
        throw new IllegalArgumentException("No empty tile could be found");
    }

    private void createNSet(Tile tile, List<Tile> visitedNeighbours){
        int id = uniqueID.id();

        existingSets.put(id, new NSet(id, tile));

        meshGraph.addNode(id);

        setTileInt(tile, id);

        if (visitedNeighbours == null || visitedNeighbours.size() == 0)
            return;

        for (Tile neighbour : visitedNeighbours){
            NSet nSet = getSet(neighbour);

            if (nSet != null){
                meshGraph.addConnection(id, nSet.getId(), 1);
            }
        }
    }

    private void addTileToNSet(Tile tileToAdd, Tile tileInNSet){
        NSet nSet = getSet(tileInNSet);

        if (nSet != null){
            nSet.add(tileToAdd);
            setTileInt(tileToAdd, nSet.getId());
        } else {
            throw new IllegalArgumentException("tileInNSet parameter is not in an existing set");
        }
    }

    private NSet getSet(Tile tile){
        for (NSet nSet : existingSets.values()){
            if (nSet.contains(tile))
                return nSet;
        }
        return null;
    }

    private List<Tile> form3x3(Tile tile){
        List<Tile> surround = new ArrayList<>();

        for (int row = -1; row < 2; row ++){
            for (int column = -1; column <2; column++){
                surround.add(tile.add(column, row));
            }
        }

        return surround;
    }

    private List<List<Tile>> form2x2s(List<Tile> surround){
        List<List<Tile>> squares = new ArrayList<>();

        squares.add(formSquare(surround, 0, 1, 3, 4));
        squares.add(formSquare(surround, 1, 2, 4, 5));
        squares.add(formSquare(surround, 3, 4, 6, 7));
        squares.add(formSquare(surround, 4, 5, 7, 8));

        return squares;
    }

    private List<Tile> formSquare(List<Tile> surround, int...ids){
        List<Tile> list = new ArrayList<>();

        try {
            if (ids != null) {
                for (int id : ids) {
                    list.add(surround.get(id));
                }
            }

        } catch (Exception e){
            Log.d("hb::MeshConstructor:formSquare", e.getMessage());
        }

        return list;
    }

    private List<List<Tile>> getSquaresWithNeighbour(Tile neighbour, List<List<Tile>> squares){
        List<List<Tile>> squaresWN = new ArrayList<>();

        for (List<Tile> square : squares){
            if (square.contains(neighbour)){
                squaresWN.add(square);
            }
        }
        return squaresWN;
    }

    private List<Tile> getNeighbours(Tile tile){
        List<Tile> neighbours = new ArrayList<>();

        Tile x = tile.add(0,1);
        addIfVisited(x, neighbours);

        x = tile.add(0,-1);
        addIfVisited(x, neighbours);

        x = tile.add(1,0);
        addIfVisited(x, neighbours);

        x = tile.add(-1,0);
        addIfVisited(x, neighbours);

        return neighbours;
    }

    private void addIfVisited(Tile tile, List<Tile> neighbours){
        if (getTileInt(tile) > 0)
            neighbours.add(tile);
    }

    private List<Tile> getBlocked(List<Tile> tiles) {
        List<Tile> blocked = new ArrayList<>();

        for (Tile tile : tiles) {
            if (getTileInt(tile) < 0) {
                blocked.add(tile);
            }
        }

        return blocked;
    }

    private boolean tilesCanBeInSameSet(List<Tile> square){
        if (square.size() != 4)
            throw new IllegalArgumentException("square parameter must have length 4");

        List<Tile> blocked = getBlocked(square);

        if (blocked.size() == 0 || blocked.size() == 3)
            return true;
        else if (blocked.size() == 1 || blocked.size() == 4)
            return false;
        else
            return !isDiagonal(blocked);
    }

    private int getTileInt(Tile tile){
        int tileInt = -1;

        try {
            tileInt = tileList.get(tile.getY()).get(tile.getX());
        } catch (Exception e){
            //do nothing, tile must not exist, return blocked value
        }

        return tileInt;
    }

    private void setTileInt(Tile tile, int id){
        tileList.get(tile.getY()).set(tile.getX(), id);
    }

    private boolean isDiagonal(List<Tile> blocked){
        if (blocked.size() == 2){
            Tile first = blocked.get(0);
            Tile second = blocked.get(1);

            return first.getX() != second.getX() && first.getY() != second.getY();
        }
        return false;
    }

    private void addEmptyNeighboursToFrontier(List<Tile> surround){
        for (Tile tile : surround){
            if (getTileInt(tile) == 0){
                System.out.println(tile + " added to frontier");
                if (!frontier.contains(tile))
                    frontier.add(tile);
            }
        }
    }

    private void makeConnections(Tile currentTile, List<Tile> visitedNeighbours){
        for (Tile neighbour : visitedNeighbours){
            if (getTileInt(currentTile) != getTileInt(neighbour))
                meshGraph.addConnection(getTileInt(currentTile), getTileInt(neighbour), 1);
        }
    }
}