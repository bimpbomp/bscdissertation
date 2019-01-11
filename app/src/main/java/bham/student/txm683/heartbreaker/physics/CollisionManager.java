package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.ArrayList;

public class CollisionManager {
    private static final String TAG = "hb::CollisionManager";
    /*
    * TODO: 3 step collision detection
    * 1. Broad Phase Spatial Partitioning
    * 2. AABB collision detection
    * 3. Object specific collision detection
    * */

    private Grid broadPhaseGrid;

    private LevelState levelState;

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;
    }

    public void checkCollisions(){
        ArrayList<ArrayList<Entity>> bins = applySpatialPartitioning();
        applySeparatingAxisTheorem(bins);
    }

    private ArrayList<ArrayList<Entity>> applySpatialPartitioning(){
        Point gridMaximum = new Point(levelState.getMap().getDimensions().first, levelState.getMap().getDimensions().second);
        broadPhaseGrid = new Grid(new Point(), gridMaximum, 70);

        broadPhaseGrid.addEntityToGrid(levelState.getPlayer());
        //Log.d(TAG, levelState.getPlayer().getName() + " added to grid");

        for (Entity entity : levelState.getNonPlayerEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
            //Log.d(TAG, entity.getName() + " added to grid");
        }

        //each element will be a bin from a grid reference with more than one entity in
        ArrayList<ArrayList<Entity>> bins = new ArrayList<>();

        for (Integer column : broadPhaseGrid.getColumnKeySet()){
            for (Integer row : broadPhaseGrid.getRowKeySet(column)){
                ArrayList<Entity> bin = broadPhaseGrid.getBin(column, row);

                if (bin.size() > 1){
                    //used to log objects in same grid cell
                    //StringBuilder sb = new StringBuilder();
                    //sb.append("Collided: ");

                    for (Entity entity : bin){
                        //sb.append(entity.getName());
                        //sb.append(", ");
                        entity.setCollided(true);
                    }
                    //Log.d(TAG, sb.toString());
                    bins.add(bin);
                }
            }
        }
        return bins;
    }

    private void applySeparatingAxisTheorem(ArrayList<ArrayList<Entity>> bins){

        for (ArrayList<Entity> bin : bins){


        }
    }

    public Grid getBroadPhaseGrid() {
        return broadPhaseGrid;
    }
}