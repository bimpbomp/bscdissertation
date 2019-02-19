package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Vector;

public class FleeFromTarget extends BNode {

    @Override
    public void init(BContext context) {

    }

    @Override
    public Status process(BContext context) {
        if (context.containsKey(BContext.FLEE_FROM) && context.containsKey(BContext.HOST_ENTITY)){
            Entity entity = (Entity) context.getValue(BContext.FLEE_FROM);

            AIEntity controlledEntity = ((AIEntity) context.getValue(BContext.HOST_ENTITY));
            Log.d("hb::FleeFromTarget", "fleeing: " + new Vector(controlledEntity.getCenter(), entity.getCenter()).sMult(-1f).toString());

            controlledEntity.setRequestedMovementVector(new Vector(controlledEntity.getCenter(), entity.getCenter()).sMult(-1f).getUnitVector());

            return Status.SUCCESS;
        }
        return Status.FAILURE;
    }
}