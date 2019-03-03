package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.ArrayList;
import java.util.List;

public class MeshConstructorV2 {

    private List<MeshSet> meshIntersectionSets;

    private Graph<Integer> meshGraph;
    private List<List<Integer>> tileList;

    private List<MeshSet> hScan;
    private List<MeshSet> vScan;

    private UniqueID uniqueID;

    @SuppressLint("UseSparseArrays")
    public MeshConstructorV2(){

        this.meshIntersectionSets = new ArrayList<>();

        meshGraph = new Graph<>();

        hScan = new ArrayList<>();
        vScan = new ArrayList<>();

        uniqueID = new UniqueID(1);
    }

    public void constructMesh(List<List<Integer>> tileList){
        this.tileList = tileList;

        hScan();

        for (MeshSet meshSet : hScan){
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(meshSet.getId());
            stringBuilder.append(": ");

            for (Tile tile : meshSet.getContainedTiles()){
                stringBuilder.append(tile);
                stringBuilder.append(", ");
            }

            stringBuilder.append("END");

            //Log.d("hb::HSCAN", stringBuilder.toString());
            System.out.println(stringBuilder.toString());
        }
    }

    private void hScan(){

        List<MeshSet> previousMeshSets = new ArrayList<>();

        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){

            List<Integer> row = tileList.get(rowIdx);
            List<MeshSet> currentMeshSets = new ArrayList<>();

            Tile startingTile = findOpenCellOnRow(rowIdx, 0);

            //whilst the row has no unchecked starting tiles
            while (startingTile != null) {
                System.out.println("starting tile: " + startingTile);
                //the row must have a non blocked cell

                //the distance to the next wall block after the startingTile in this row
                int distanceToWall = getHDistanceToWall(startingTile.getX(), row);

                //the meshset currently being added to
                MeshSet activeMeshSet = getActiveMeshSetHScan(startingTile, distanceToWall, previousMeshSets);
                currentMeshSets.add(activeMeshSet);

                //at this point, we have the meshset to add cells to, so walk along the row from the starting cell (inclusive)
                //until a wall is hit.

                int currentCell;
                //the column index that the scan loop exited at
                int loopExitColumnValue = row.size()-1;

                //scan loop
                for (int columnIdx = startingTile.getX(); columnIdx < row.size(); columnIdx++) {
                    currentCell = row.get(columnIdx);
                    loopExitColumnValue = columnIdx;

                    if (currentCell == 0) {
                        //is empty, add to activeMeshSet
                        activeMeshSet.add(new Tile(columnIdx, rowIdx));
                    } else {
                        //is not empty, break
                        break;
                    }
                }

                //if the exit value of the scan loop didnt reach the end of the row, then find the next starting tile
                //if it did, then the row is finished, setting startingTile to null will end the loop for this row.
                if (loopExitColumnValue < row.size()-1)
                    startingTile = findOpenCellOnRow(rowIdx, loopExitColumnValue);
                else
                    startingTile = null;

            }


            //iteration is finished, moving onto next row. save state of this row
            previousMeshSets = currentMeshSets;
        }
    }

    private MeshSet getActiveMeshSetHScan(Tile startingTile, int distanceToWall, List<MeshSet> previousMeshSets){
        MeshSet activeMeshSet = null;

        if (previousMeshSets.size() > 0){
            //if a previous meshset shared a start tile, and has the same distance to a cell, the current row section
            //can be a part of it

            for (MeshSet meshSet : previousMeshSets){
                if (meshSet.startTile.getX() == startingTile.getX() && meshSet.distanceToWall == distanceToWall){
                    activeMeshSet = meshSet;
                    break;
                }
            }

        }

        if (activeMeshSet == null){
            //either previous row didnt have any meshsets or the previous row doesnt have any compatible meshsets
            activeMeshSet = new MeshSet(uniqueID.id(), startingTile, distanceToWall);
        }
        hScan.add(activeMeshSet);

        return activeMeshSet;
    }

    private int getHDistanceToWall(int startingColumn, List<Integer> row){
        int count = 0;
        for (int columnIdx = startingColumn; columnIdx < row.size(); columnIdx++){

            int cell = row.get(columnIdx);

            if (cell == 0){
                count++;
            } else {
                return count;
            }
        }
        return count;
    }

    //for hScan
    private Tile findOpenCellOnRow(int rowIdx, int startingCol){
        if (rowIdx > tileList.size()-1)
            throw new IllegalArgumentException("rowIdx: " + rowIdx + " out of range: " + (tileList.size()-1));

        List<Integer> row = tileList.get(rowIdx);
        System.out.print("row: ");
        row.forEach(System.out::print);
        System.out.println();

        for (int columnIdx = startingCol; columnIdx < row.size(); columnIdx++){
            int cell = row.get(columnIdx);

            if (cell == 0){
                System.out.println("found: " + new Tile(columnIdx, rowIdx));
                return new Tile(columnIdx, rowIdx);
            }
        }
        //no open cells
        return null;
    }

    //for vScan
    private Tile findOpenCellOnColumn(int columnIdx){

        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){

            try {
                if (tileList.get(rowIdx).get(columnIdx) == 0)
                    return new Tile(columnIdx, rowIdx);

            } catch (IndexOutOfBoundsException e){
                //do nothing
            }
        }
        return null;
    }

    private void intersectScans(List<MeshSet> scanMesh1, List<MeshSet> scanMesh2){
        UniqueID meshIdGen = new UniqueID(1);

        for (MeshSet meshSet : scanMesh1){
            for (MeshSet secondMeshSet : scanMesh2){
                meshIntersectionSets.add(new MeshSet(meshIdGen.id(), meshSet.intersection(secondMeshSet)));
            }
        }
    }

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

        public int getId() {
            return id;
        }

        public Tile getStart(){
            return startTile;
        }

        public boolean sameDistanceToWall(int distToWall){
            return this.distanceToWall == distToWall;
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
}