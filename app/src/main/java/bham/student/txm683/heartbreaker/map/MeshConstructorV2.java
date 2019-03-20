package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.util.Log;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.ArrayList;
import java.util.List;

class MeshConstructorV2 {

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

    @SuppressLint("UseSparseArrays")
    MeshConstructorV2(){

        this.meshIntersectionSets = new ArrayList<>();

        meshGraph = new Graph<>();

        hScan = new ArrayList<>();
        vScan = new ArrayList<>();

        meshSetId = new UniqueID(1);
        intersectionId = new UniqueID(1);
    }

    List<MeshPolygon> getMeshPolygons(int tileSize){
        List<MeshPolygon> meshPolygons = new ArrayList<>();

        for (MeshSet meshSet : meshIntersectionSets){
            meshPolygons.add(new MeshPolygon(meshSet, tileSize));
        }

        return meshPolygons;
    }

    Graph<Integer> getMeshGraph() {
        return meshGraph;
    }

    /**
     * Constructs meshSets from given tileList.
     * Assumes that the tileList is properly padded so all columns are aligned
     * @param tileList Grid of integers stating their availability. 0 = free, -1 = wall, MININT = column padding
     */
    void constructMesh(List<List<Integer>> tileList, List<DoorBuilder> doorBuilders){
        this.tileList = tileList;

        currentScan = Scan.HORIZONTAL;
        hScan();

        currentScan = Scan.VERTICAL;
        vScan();

        meshSetListPrint("MESHPRINT HSCAN",hScan);

        meshSetListPrint("MESHPRINT VSCAN", vScan);

        intersectScans(hScan, vScan);

        meshSetListPrint("MESHPRINT INTERSECTION", meshIntersectionSets);


        //make sure doors are in their own set
        for (DoorBuilder doorBuilder : doorBuilders){
            Tile doorTile = doorBuilder.getLiesOn();

            MeshSet doorSet = null;

            for (MeshSet meshSet : meshIntersectionSets){
                List<Tile> containedTiles = meshSet.getContainedTiles();

                if (containedTiles.contains(doorTile) && containedTiles.size() > 1){
                    meshSet.removeTile(doorTile);

                    doorSet = new MeshSet(intersectionId.id(), doorTile, 0);
                }
            }

            if (doorSet != null){
                meshIntersectionSets.add(doorSet);
                meshGraph.addNode(doorSet.getId());
            }
        }

        /*//put any doors into their own set
        for (int i = 0; i < tileList.size(); i++){
            List<Integer> row = tileList.get(i);
            for (int j = 0; j < row.size(); j++){
                Tile tile = new Tile(j, i);
                if (getTileInt(tile) == -2){
                    for (MeshSet meshSet : meshIntersectionSets){
                        if (meshSet.hasTile(tile)){
                            if (meshSet.getContainedTiles().size() > 1){

                            }
                        }
                    }
                }
            }
        }*/

        constructGraph();

        Log.d("hb::MESHGRAPH", meshGraph.toString());
    }

    private void constructGraph(){

        //change tileList to store id of the set they belong to
        for (int rowIdx = 0; rowIdx < tileList.size(); rowIdx++){
            List<Integer> row = tileList.get(rowIdx);

            for (int columnIdx = 0; columnIdx < row.size(); columnIdx++){
                //makeConnectionsToVisitedNeighbours(new Tile(columnIdx, rowIdx));
                for (MeshSet meshSet : meshIntersectionSets){
                    Tile tile = new Tile(columnIdx, rowIdx);
                    if (meshSet.hasTile(tile))
                        setTileInt(tile, meshSet.getId());
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

    /*public void addPointsToLineToNeighbour(int neighbourId, Point... points){
        Map<Integer, Line> edgesToNeighbours =
        if (points == null || points.length == 0){
            return;
        }

        if (!edgesToNeighbours.containsKey(neighbourId)){
            edgesToNeighbours.put();
        }

        for (Point point : points){

        }
    }*/

    private void addIfVisited(Tile tile, Tile neighbour){
        int tileInt = getTileInt(tile);
        int neighbourInt = getTileInt(neighbour);

        if (neighbourInt > 0 && tileInt != neighbourInt){
            //if neighbour has been visited, add a connection both ways
            meshGraph.addConnection(tileInt, neighbourInt,1);
            meshGraph.addConnection(neighbourInt, tileInt, 1);
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

                //the meshset currently being added to
                MeshSet activeMeshSet = getActiveMeshSet(startingTile, distanceToWall, previousMeshSets);
                currentMeshSets.add(activeMeshSet);

                //at this point, we have the meshset to add cells to, so walk along the row from the starting cell (inclusive)
                //until a wall is hit.

                //the column index that the scan loop exited at

                //int loopExitRowValue = tileList.size()-1;
                ExitValue loopExitValue = new ExitValue(tileList.size()-1);
                int currentCell;

                if (tileList.get(startingTile.getY()).get(columnIdx) == -2) {
                    Log.d("MESHV", "door found at: " + new Tile(columnIdx, startingTile.getY()));
                    //starting tile is a door
                    addToMeshSet(activeMeshSet, startingTile);
                    loopExitValue.setExitVal(startingTile.getY() + 1);
                } else {
                    //scan loop
                    for (int rowIdx = startingTile.getY(); rowIdx < tileList.size(); rowIdx++) {
                        currentCell = tileList.get(rowIdx).get(columnIdx);

                        Log.d("MESHVS", "looking at: " + new Tile(columnIdx, rowIdx));
                        //loopExitRowValue = rowIdx;
                        loopExitValue.setExitVal(rowIdx);

                        if (analyseCell(currentCell, new Tile(columnIdx, rowIdx), activeMeshSet, loopExitValue))
                            break;
                    }
                }

                //if the exit value of the scan loop didnt reach the end of the row, then find the next starting tile
                //if it did, then the row is finished, setting startingTile to null will end the loop for this row.
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

                //the distance to the next wall block after the startingTile in this row
                int distanceToWall = getHDistanceToWall(startingTile.getX(), row);

                //the meshset currently being added to
                MeshSet activeMeshSet = getActiveMeshSet(startingTile, distanceToWall, previousMeshSets);
                currentMeshSets.add(activeMeshSet);

                //at this point, we have the meshset to add cells to, so walk along the row from the starting cell (inclusive)
                //until a wall is hit.

                int currentCell;
                //the column index that the scan loop exited at

                //int loopExitColumnValue = row.size()-1;
                ExitValue loopExitValue = new ExitValue(row.size()-1);

                if (/*row.get(startingTile.getX()) == -2*/false){
                    //starting tile is a door
                    addToMeshSet(activeMeshSet, startingTile);
                    loopExitValue.setExitVal(startingTile.getX()+1);
                } else {
                    //scan loop
                    for (int columnIdx = startingTile.getX(); columnIdx < row.size(); columnIdx++) {
                        currentCell = row.get(columnIdx);
                        //loopExitColumnValue = columnIdx;
                        loopExitValue.setExitVal(columnIdx);

                        if (analyseCell(currentCell, new Tile(columnIdx, rowIdx), activeMeshSet, loopExitValue))
                            break;

                    }
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

    private boolean analyseCell(int cell, Tile tile, MeshSet activeMeshSet, ExitValue exitValue){
        if (cell == 0 || cell == -2) {
            //is empty, add to activeMeshSet
            addToMeshSet(activeMeshSet, tile);

            return false;
        } /*else if (cell == -2){
            //the cell is a door, add to it's own set, applyMovementForces on
            //addToMeshSet(activeMeshSet, tile);
            getActiveMeshSet(tile, 0, new ArrayList<>());
            exitValue.increment();
        }*/

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