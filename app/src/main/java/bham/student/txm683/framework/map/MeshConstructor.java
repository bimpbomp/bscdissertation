package bham.student.txm683.framework.map;

import android.annotation.SuppressLint;
import android.util.Log;
import bham.student.txm683.framework.utils.Tile;
import bham.student.txm683.framework.utils.UniqueID;
import bham.student.txm683.framework.utils.Vector;
import bham.student.txm683.framework.utils.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshConstructor {

    private List<MeshSet> meshIntersectionSets;

    private Graph<Integer> meshGraph;
    private List<List<Integer>> tileList;

    private List<MeshSet> hScan;
    private List<MeshSet> vScan;

    private UniqueID meshSetId;
    private UniqueID intersectionId;

    private enum Scan {
        VERTICAL,
        HORIZONTAL
    }

    private Scan currentScan;

    private Map<Integer, MeshPolygon> meshPolygons;

    private int tileSize;

    @SuppressLint("UseSparseArrays")
    public MeshConstructor(){

        this.meshIntersectionSets = new ArrayList<>();

        meshGraph = new Graph<>();

        hScan = new ArrayList<>();
        vScan = new ArrayList<>();

        meshSetId = new UniqueID(1);
        intersectionId = new UniqueID(1);

        meshPolygons = new HashMap<>();
    }

    private void constructMeshPolygons(){

        for (MeshSet meshSet : meshIntersectionSets){
            MeshPolygon meshPolygon = new MeshPolygon(meshSet, tileSize);

            meshPolygons.put(meshPolygon.getId(), meshPolygon);
        }
    }

    public List<MeshPolygon> getMeshPolygons(){
        return new ArrayList<>(meshPolygons.values());
    }

    public Graph<Integer> getMeshGraph() {
        return meshGraph;
    }

    /**
     * Constructs meshSets from given tileList.
     * Assumes that the tileList is properly padded so all columns are aligned
     * @param tileList Grid of integers stating their availability. 0 = free, -1 = wall, MININT = column padding
     */
    public void constructMesh(List<List<Integer>> tileList, List<DoorBuilder> doorBuilders, int tileSize){
        this.tileList = tileList;
        this.tileSize = tileSize;

        currentScan = Scan.HORIZONTAL;
        hScan();

        currentScan = Scan.VERTICAL;
        vScan();

        intersectScans(hScan, vScan);

        //make sure doors are in their own set by iterating through the door spawn cells
        for (DoorBuilder doorBuilder : doorBuilders){
            Tile doorTile = doorBuilder.getLiesOn();

            MeshSet doorSet = null;

            //find the meshset that the door is in
            for (MeshSet meshSet : meshIntersectionSets){
                List<Tile> containedTiles = meshSet.getContainedTiles();

                //if the meshset has tiles other than the door tile
                if (containedTiles.contains(doorTile) && containedTiles.size() > 1){
                    //remove the door from the set
                    meshSet.removeTile(doorTile);

                    //create a new set for the door
                    doorSet = new MeshSet(intersectionId.id(), doorTile, 0);
                }
            }

            if (doorSet != null){
                //if the door was added to a new set, add this new set to the graph as a node,
                //and to the intersection sets
                meshIntersectionSets.add(doorSet);
                meshGraph.addNode(doorSet.getId());
            }
        }

        constructMeshPolygons();
        constructGraph();
    }

    private void constructGraph(){

        //change cell to store id of the set they belong to

        //iterate through the rows
        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){
            List<Integer> row = tileList.get(rowIdx);

            //iterate through each cell in a row
            for (int columnIdx = 0; columnIdx < row.size(); columnIdx++){

                //find the meshset with the current tile's coordinates
                for (MeshSet meshSet : meshIntersectionSets){
                    Tile tile = new Tile(columnIdx, rowIdx);
                    if (meshSet.hasTile(tile)) {
                        setTileInt(tile, meshSet.getId());
                        break;
                    }
                }
            }
        }

        //form connections between neighbouring sets
        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){
            List<Integer> row = tileList.get(rowIdx);

            for (int columnIdx = 0; columnIdx < row.size(); columnIdx++){
                makeConnectionsToVisitedNeighbours(new Tile(columnIdx, rowIdx));
            }
        }
    }

    private void makeConnectionsToVisitedNeighbours(Tile tile){

        Tile x = tile.add(0,1);
        addIfVisited(tile, x);

        x = tile.add(0,-1);
        addIfVisited(tile, x);

        x = tile.add(1,0);
        addIfVisited(tile, x);

        x = tile.add(-1,0);
        addIfVisited(tile, x);
    }

    private void addIfVisited(Tile tile, Tile neighbour){
        int tileInt = getTileInt(tile);
        int neighbourInt = getTileInt(neighbour);

        if (neighbourInt > 0 && tileInt != neighbourInt && tileInt > 0){
            Log.d("GRAPH", "map size: "+ meshPolygons.values().size());
            Log.d("GRAPH", "map size: "+ meshPolygons.values().size() + ", " + (meshPolygons.get(tileInt).getCenter() == null));
            Log.d("GRAPH", ""+(meshPolygons.get(neighbourInt).getCenter() == null));
            int weight = (int) new Vector(meshPolygons.get(tileInt).getCenter(), meshPolygons.get(neighbourInt).getCenter()).getLength();

            weight = weight / tileSize;

            //if neighbour has been visited, add a connection both ways
            meshGraph.addConnection(tileInt, neighbourInt,weight);
            meshGraph.addConnection(neighbourInt, tileInt, weight);
        }
    }

    private int getTileInt(Tile tile){
        int tileInt = Integer.MIN_VALUE;

        try {
            tileInt = tileList.get(tile.getY()).get(tile.getX());
        } catch (Exception e){
            //do nothing, tile must not exist, return blocked value
        }

        return tileInt;
    }

    private void meshSetListPrint(String tag, List<MeshSet> meshSets){
        for (MeshSet meshSet : meshSets){
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(meshSet.getId());
            stringBuilder.append(": ");

            for (Tile tile : meshSet.getContainedTiles()){
                stringBuilder.append(tile);
                stringBuilder.append(", ");
            }

            stringBuilder.append("END");

            Log.d("hb::"+tag, stringBuilder.toString());
        }
    }

    private void vScan(){

        List<MeshSet> previousMeshSets = new ArrayList<>();

        for (int columnIdx = 0; columnIdx < getMaxColumnLength(); columnIdx++){

            List<MeshSet> currentMeshSets = new ArrayList<>();

            Tile startingTile = findOpenCellOnColumn(columnIdx, 0);

            //whilst the row has no unchecked starting tiles
            while (startingTile != null) {
                //the row must have a non blocked cell

                //the distance to the next wall block after the startingTile in this row
                int distanceToWall = getVDistanceToWall(startingTile.getY(), columnIdx);

                //getActiveMeshSet checks if there are any meshsets in the previousMeshSets list that comply
                //with the rules outlined in the design section
                MeshSet activeMeshSet = getActiveMeshSet(startingTile, distanceToWall, previousMeshSets);
                currentMeshSets.add(activeMeshSet);

                //at this point, we have the meshset to add cells to, so walk along the row
                //from the starting cell (inclusive) until a wall is hit.

                //the row index that the scan loop exited at,
                //so we can resume the loop right after the current section

                int currentCell;
                ExitValue loopExitValue = new ExitValue(tileList.size()-1);

                if (tileList.get(startingTile.getY()).get(columnIdx) == -2) {
                    //starting tile is a door, add it to the active meshset
                    addToMeshSet(activeMeshSet, startingTile);
                    loopExitValue.setExitVal(startingTile.getY() + 1);
                } else {
                    //scan loop
                    for (int rowIdx = startingTile.getY(); rowIdx < tileList.size(); rowIdx++) {
                        currentCell = tileList.get(rowIdx).get(columnIdx);

                        loopExitValue.setExitVal(rowIdx);

                        if (analyseCell(currentCell, new Tile(columnIdx, rowIdx), activeMeshSet))
                            break;
                    }
                }

                //if the exit value of the scan loop didnt reach the end of the column, then find the next starting tile
                //if it did, then the row is finished, setting startingTile to null will end the loop for this column.
                if (loopExitValue.getExitVal() < tileList.size()-1)
                    startingTile = findOpenCellOnColumn(columnIdx, loopExitValue.getExitVal());
                else
                    startingTile = null;

            }

            //iteration is finished, moving onto next row. save state of this row
            previousMeshSets = currentMeshSets;
        }
    }

    private void addToMeshSet(MeshSet meshSet, Tile tile){
        meshSet.add(tile);
    }

    private void setTileInt(Tile tile, int id){
        tileList.get(tile.getY()).set(tile.getX(), id);
    }

    private void hScan(){

        List<MeshSet> previousMeshSets = new ArrayList<>();

        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){

            List<Integer> row = tileList.get(rowIdx);
            List<MeshSet> currentMeshSets = new ArrayList<>();

            Tile startingTile = findOpenCellOnRow(rowIdx, 0);

            //whilst the row has no unchecked starting tiles
            while (startingTile != null) {
                //the row must have a non blocked cell

                //the distance to the next blocked cell after the startingTile in this row
                int distanceToWall = getHDistanceToWall(startingTile.getX(), row);

                //the meshset currently being added to
                MeshSet activeMeshSet = getActiveMeshSet(startingTile, distanceToWall, previousMeshSets);
                currentMeshSets.add(activeMeshSet);

                //at this point, we have the meshset to add cells to, so walk along the row
                //from the starting cell (inclusive) until a wall is hit.

                //the column index that the scan loop exited at,
                //so we can resume the loop right after the current section
                ExitValue loopExitValue = new ExitValue(row.size()-1);

                int currentCell;
                for (int columnIdx = startingTile.getX(); columnIdx < row.size(); columnIdx++) {
                    currentCell = row.get(columnIdx);
                    //loopExitColumnValue = columnIdx;
                    loopExitValue.setExitVal(columnIdx);

                    if (analyseCell(currentCell, new Tile(columnIdx, rowIdx), activeMeshSet))
                        break;

                }

                //if the exit value of the scan loop didnt reach the end of the row, then find the next starting tile
                //if it did, then the row is finished, setting startingTile to null will end the loop for this row.
                if (loopExitValue.getExitVal() < row.size()-1)
                    startingTile = findOpenCellOnRow(rowIdx, loopExitValue.getExitVal());
                else
                    startingTile = null;

            }

            //iteration is finished, moving onto next row. save state of this row
            previousMeshSets = currentMeshSets;
        }
    }


    private boolean analyseCell(int cell, Tile tile, MeshSet activeMeshSet){
        if (cell == 0 || cell == -2) {
            //is empty or a door, add to activeMeshSet
            addToMeshSet(activeMeshSet, tile);
            return false;
        }
        return true;
    }

    private MeshSet getActiveMeshSet(Tile startingTile, int distanceToWall, List<MeshSet> previousMeshSets){
        MeshSet activeMeshSet = null;

        boolean setAlreadyExists = false;
        if (previousMeshSets.size() > 0){
            //if a previous meshset shared a start tile, and has the same distance to a cell, the current row section
            //can be a part of it

            for (MeshSet meshSet : previousMeshSets){
                if (meshSet.getDistanceToWall() == distanceToWall){

                    //checks different component of MeshSet depending on orientation of scan
                    if (currentScan == Scan.HORIZONTAL && meshSet.getStartTile().getX() == startingTile.getX() ||
                            currentScan == Scan.VERTICAL && meshSet.getStartTile().getY() == startingTile.getY()) {

                        activeMeshSet = meshSet;
                        setAlreadyExists = true;
                        break;
                    }
                }
            }

        }

        if (activeMeshSet == null){
            //either previous row didnt have any meshsets or the previous row doesnt have any compatible meshsets
            activeMeshSet = new MeshSet(meshSetId.id(), startingTile, distanceToWall);
        }

        if (!setAlreadyExists) {
            if (currentScan == Scan.HORIZONTAL) {
                hScan.add(activeMeshSet);
            } else
                vScan.add(activeMeshSet);
        }

        return activeMeshSet;
    }

    private int getHDistanceToWall(int startingColumn, List<Integer> row){
        int count = 0;
        for (int columnIdx = startingColumn; columnIdx < row.size(); columnIdx++){

            int cell = row.get(columnIdx);

            if (cell == 0 || cell == -2){
                count++;
            } else {
                return count;
            }
        }
        return count;
    }

    private int getVDistanceToWall(int startingRow, int columnIdx){
        int count = 0;

        for (int rowIdx = startingRow; rowIdx < tileList.size(); rowIdx++){
            int cell = tileList.get(rowIdx).get(columnIdx);

            if (cell == 0 || cell == -2)
                count++;
            else
                return count;
        }
        return count;
    }

    //for hScan
    private Tile findOpenCellOnRow(int rowIdx, int startingCol){
        if (rowIdx > tileList.size()-1)
            throw new IllegalArgumentException("rowIdx: " + rowIdx + " out of range: " + (tileList.size()-1));

        List<Integer> row = tileList.get(rowIdx);

        for (int columnIdx = startingCol; columnIdx < row.size(); columnIdx++){
            int cell = row.get(columnIdx);

            if (cell == 0 || cell == -2){
                return new Tile(columnIdx, rowIdx);
            }
        }
        //no open cells
        return null;
    }

    private int getMaxColumnLength(){
        int maxColLength = 0;

        for (List<Integer> row : tileList){
            if (row.size() > maxColLength)
                maxColLength = row.size();
        }
        return maxColLength;
    }

    //for vScan
    private Tile findOpenCellOnColumn(int columnIdx, int startingRow){

        for (int rowIdx = startingRow; rowIdx < tileList.size(); rowIdx++){

            try {
                int cell = tileList.get(rowIdx).get(columnIdx);
                if (cell == 0 || cell == -2)
                    return new Tile(columnIdx, rowIdx);

            } catch (IndexOutOfBoundsException e){
                //do nothing
            }
        }
        return null;
    }

    private void intersectScans(List<MeshSet> scanMesh1, List<MeshSet> scanMesh2){

        for (MeshSet meshSet : scanMesh1){
            for (MeshSet secondMeshSet : scanMesh2){
                List<Tile> intersection = meshSet.intersection(secondMeshSet);

                if (intersection.size() > 0) {
                    int newId = intersectionId.id();
                    meshIntersectionSets.add(new MeshSet(newId, intersection));
                    meshGraph.addNode(newId);
                }
            }
        }
    }

    private class ExitValue {
        private int exitVal;

        ExitValue(int exitVal){
            this.exitVal = exitVal;
        }

        public int getExitVal() {
            return exitVal;
        }

        public void setExitVal(int exitVal) {
            this.exitVal = exitVal;
        }

        public void increment(){
            this.exitVal++;
        }
    }
}