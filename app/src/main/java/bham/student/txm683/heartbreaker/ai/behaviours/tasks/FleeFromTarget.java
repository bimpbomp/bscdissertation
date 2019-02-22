package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.TileSet;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.PathFinding;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class FleeFromTarget extends BNode {

    private Queue<Tile> path;

    private AIEntity controlledEntity;
    private LevelState levelState;
    private Entity entityFleeingFrom;

    public FleeFromTarget(){
        path = new LinkedList<>();
    }

    @Override
    public void init(BContext context) {
        //do pathfinding
        levelState = (LevelState) context.getValue(BContext.LEVEL_STATE);
        controlledEntity = (AIEntity) context.getValue(BContext.HOST_ENTITY);

        path = new LinkedList<>(PathFinding.bfsFlee(levelState.getTileSet(), controlledEntity.getCenter()));

        StringBuilder stringBuilder = new StringBuilder();

        path.forEach((temp) -> stringBuilder.append(temp.toString()+", "));

        Log.d("hb::Flee", stringBuilder.toString());
    }

    @Override
    public Status process(BContext context) {
        if (context.containsKey(BContext.FLEE_FROM) && context.containsKey(BContext.HOST_ENTITY)){
            entityFleeingFrom = (Entity) context.getValue(BContext.FLEE_FROM);

            controlledEntity = (AIEntity) context.getValue(BContext.HOST_ENTITY);

            TileSet tileSet = ((LevelState) context.getValue(BContext.LEVEL_STATE)).getTileSet();

            if (!path.isEmpty()) {
                //if there are nodes left to travel
                if (path.peek().equals(Tile.mapToCenterOfTile(controlledEntity.getCenter(), tileSet.getTileSize()))) {
                    //if the entity has reached the current point on the path, remove it from the list
                    path.poll();
                }

                if (!path.isEmpty()) {
                    //if the path is still not empty, then head to next node
                    controlledEntity.setRequestedMovementVector(new Vector(controlledEntity.getCenter(), new Point(path.peek())).getUnitVector());
                    return Status.RUNNING;
                }
            }

            //no more nodes on path, check if player is visible, if not, success, else failure
            if (!tileSet.tileIsVisibleToPlayer(Tile.mapToCenterOfTile(controlledEntity.getCenter(), tileSet.getTileSize()))){
                return Status.SUCCESS;
            } else {
                return Status.FAILURE;
            }

        }
        return Status.FAILURE;
    }
}

/*
Entity entity = (Entity) context.getValue(BContext.FLEE_FROM);

AIEntity controlledEntity = ((AIEntity) context.getValue(BContext.HOST_ENTITY));
        Log.d("hb::FleeFromTarget", "fleeing: " + new Vector(controlledEntity.getCenter(), entity.getCenter()).sMult(-1f).toString());

                controlledEntity.setRequestedMovementVector(new Vector(controlledEntity.getCenter(), entity.getCenter()).sMult(-1f).getUnitVector());

                return Status.SUCCESS;*/
