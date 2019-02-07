package bham.student.txm683.heartbreaker.physics;

import android.util.Pair;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Grid {
    private static final String TAG = "hb::Grid";

    private TreeMap<Integer, TreeMap<Integer, ArrayList<Collidable>>> grid;

    private Point gridMinimum;
    private Point gridMaximum;

    private Pair<Integer, Integer> gridDimensionsInCells;

    private int cellSize;

    public Grid(Point gridMinimum, Point gridMaximum, int cellSize) {
        this.gridMinimum = gridMinimum;
        this.gridMaximum = gridMaximum;

        this.cellSize = cellSize;

        int widthInCells = (int) Math.ceil((gridMaximum.getX()-gridMinimum.getX())/this.cellSize);
        int heightInCells = (int) Math.ceil((gridMaximum.getY()-gridMinimum.getY())/this.cellSize);

        this.gridDimensionsInCells = new Pair<>(widthInCells, heightInCells);

        this.grid = new TreeMap<>();
    }


    void addEntityToGrid(Collidable entity){
        //Initial method: add entity to the cell that each of it's AABB vertices exist in.

        Point[] vertices = entity.getCollisionVertices();

        //contains unique grid references
        Set<Pair<Integer, Integer>> addedGridReferences = new HashSet<>();

        //adds entities vertices to grid
        for (Point point : vertices){
            Pair<Integer, Integer> pointGridReference = mapPointToGridPosition(point);

            addedGridReferences.add(pointGridReference);

            //Log.d(TAG, point.toString() + " maps to: " + pointGridReference.first + ", " + pointGridReference.second);
        }


        for (Pair<Integer, Integer> gridReference : addedGridReferences){
            if ((gridReference.first >= 0 && gridReference.first <= gridDimensionsInCells.first) && (gridReference.second >= 0 && gridReference.second <= gridDimensionsInCells.second))
                insertEntityAtGridPosition(gridReference.first, gridReference.second, entity);
        }
    }

    private Pair<Integer, Integer> mapPointToGridPosition(Point point){
        // Math.floor( (position - gridMinimum) / gridCellSize )

        int column = (int) Math.floor((point.getX() - gridMinimum.getX()) / cellSize);
        int row = (int) Math.floor((point.getY() - gridMinimum.getY()) / cellSize);
        return new Pair<>(column, row);
    }

    private void insertEntityAtGridPosition(int column, int row, Collidable entity){
        //if grid column hasn't been accessed before, initialise it before adding entity
        if (!grid.containsKey(column)){
            grid.put(column, new TreeMap<>());
        }

        TreeMap<Integer, ArrayList<Collidable>> rowMap = grid.get(column);

        //if grid row at the given column hasn't been accessed before, initialise it before adding entity
        if (!rowMap.containsKey(row)){
            rowMap.put(row, new ArrayList<>());
        }

        //add entity to bin
        rowMap.get(row).add(entity);
    }

    ArrayList<Collidable> getBin(int column, int row){
        if (grid.containsKey(column)) {
            if (grid.get(column).containsKey(row))
                return grid.get(column).get(row);
        }
        return null;
    }

    Set<Integer> getColumnKeySet(){
        return grid.keySet();
    }

    Set<Integer> getRowKeySet(int column){
        return grid.get(column).keySet();
    }

    public Pair<Integer, Integer> getGridDimensionsInCells(){
        return gridDimensionsInCells;
    }

    public int getCellSize(){
        return cellSize;
    }

    public Point getGridMinimum() {
        return gridMinimum;
    }

    public Point getGridMaximum() {
        return gridMaximum;
    }
}