package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Vector;

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
        broadPhaseGrid = new Grid(new Vector(0,0), levelState.getMap().getDimensionVector(), 70);

        broadPhaseGrid.addEntityToGrid(levelState.getPlayer());
        //Log.d(TAG, levelState.getPlayer().getName() + " added to grid");

        for (Entity entity : levelState.getNonPlayerEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
            //Log.d(TAG, entity.getName() + " added to grid");
        }

        for (Integer column : broadPhaseGrid.getColumnKeySet()){
            for (Integer row : broadPhaseGrid.getRowKeySet(column)){
                ArrayList<Entity> bin = broadPhaseGrid.getBin(column, row);

                if (bin.size() > 1){
                    StringBuilder sb = new StringBuilder();
                    sb.append("Collided: ");

                    for (Entity entity : bin){
                        sb.append(entity.getName());
                        sb.append(", ");
                        entity.setCollided(true);
                    }
                    Log.d(TAG, sb.toString());
                }
            }
        }
    }

    public Grid getBroadPhaseGrid() {
        return broadPhaseGrid;
    }
}