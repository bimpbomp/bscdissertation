package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Idle extends BNode {

    @Override
    public void init(BContext context) {

    }

    @Override
    public Status process(BContext context) {

        //if the context has the host_entity
        if (context.containsKey(BContext.HOST_ENTITY) && context.getValue(BContext.HOST_ENTITY) instanceof AIEntity){

            Log.d("hb::"+getClass().getSimpleName(), "context has info");
            //stop moving, and rotate
            ((AIEntity) context.getValue(BContext.HOST_ENTITY)).setRequestedMovementVector(Vector.ZERO_VECTOR);
            ((AIEntity) context.getValue(BContext.HOST_ENTITY)).rotateBy(0.261799f);

            return Status.SUCCESS;
        }

        Log.d("hb::"+getClass().getSimpleName(), "context doesn't have info");

        return Status.FAILURE;
    }
}
