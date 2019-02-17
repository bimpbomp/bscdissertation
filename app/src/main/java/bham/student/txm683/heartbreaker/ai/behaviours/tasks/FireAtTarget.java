package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Shooter;
import bham.student.txm683.heartbreaker.utils.Vector;

public class FireAtTarget extends BNode {

    @Override
    public void init(BContext context) {

    }

    @Override
    public Status process(BContext context) {

        if (context.containsControlledEntity() && context.containsLevelState()
                && context.containsKey("sight_vector") && context.getValue("sight_vector") instanceof Vector){

            AIEntity controlledEntity = (AIEntity) context.getValue(BContext.CONTROLLED_ENTITY_KEY);

            //if controlled entity can't shoot, FAIL
            if (!(controlledEntity instanceof Shooter))
                return Status.FAILURE;

            Vector sightVector = (Vector) context.getValue("sight_vector");
            float dot = sightVector.getUnitVector().dot(controlledEntity.getForwardUnitVector());

            if (dot > 0 && dot > 0.8f){
                //if the target is within roughly 10 degrees of the ai's forward vector
                //controlled entity can see the target
                ((LevelState) context.getValue(BContext.LEVEL_STATE_KEY)).addBullet(((Shooter) controlledEntity).shoot());

                return Status.SUCCESS;

            } else {
                //can't see the target, rotate to see it
                controlledEntity.rotate(sightVector);

                return Status.RUNNING;
            }
        }
        return Status.FAILURE;
    }
}
