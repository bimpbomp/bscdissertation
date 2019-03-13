package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.Shooter;
import bham.student.txm683.heartbreaker.utils.Vector;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

public class FireAtTarget extends BNode {

    @Override
    public void reset(BContext context) {

    }

    @Override
    public Status process(BContext context) {

        //if the context contains the host, level_state, sight_vector, and they're of the right type
        if (context.containsKeys(CONTROLLED_ENTITY)
                && context.containsKeys(LEVEL_STATE)
                && context.containsKeys(ATTACK_TARGET)){

            //get the controlled entity
            AIEntity controlledEntity = (AIEntity) context.getValue(CONTROLLED_ENTITY);
            LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);

            //if controlled entity can't aim, FAIL
            if (!(controlledEntity instanceof Shooter))
                return Status.FAILURE;

            Vector sightVector = new Vector(controlledEntity.getCenter(), ((Entity) context.getValue(ATTACK_TARGET)).getCenter());
            float dot = sightVector.getUnitVector().dot(controlledEntity.getForwardUnitVector());

            if (dot > 0.9f){
                //if the target is within roughly 10 degrees of the ai's forward vector
                //controlled entity can see the target
                Log.d("hb::FireAtTarget", "shooting");
                levelState.addBullet(((Shooter) controlledEntity).shoot());

                return Status.SUCCESS;

            } else {
                //can't see the target, rotate to see it
                controlledEntity.rotate(sightVector);
                Log.d("hb::FireAtTarget", "rotating");

                return Status.RUNNING;
            }
        }
        return Status.FAILURE;
    }
}
