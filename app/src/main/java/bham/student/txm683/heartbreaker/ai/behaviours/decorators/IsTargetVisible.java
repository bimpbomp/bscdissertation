package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.utils.Vector;

public class IsTargetVisible extends DecoratorBNode {

    public IsTargetVisible(BNode child) {
        super(child);
    }

    @Override
    boolean condition(BContext context) {
        /*if (context.containsAttackTarget() && context.containsControlledEntity()){*/
        if (context.containsKey(BContext.ATTACK_TARGET_KEY) && context.containsKey(BContext.CONTROLLED_ENTITY_KEY)){
            Log.d("hb::IsTargetVisible", "Context contains needed info");


            Entity target = (Entity) context.getValue(BContext.ATTACK_TARGET_KEY);
            AIEntity controlledEntity = (AIEntity) context.getValue(BContext.CONTROLLED_ENTITY_KEY);

            Vector sightVector = new Vector (controlledEntity.getCenter(), target.getCenter());

            float dot = sightVector.getUnitVector().dot(controlledEntity.getForwardUnitVector());

            context.addPair("sight_vector", sightVector);

            Log.d("hb::IsTargetVisible", "sight vector length: " + sightVector.getLength());
            Log.d("hb::IsTargetVisible", "dot product: " + dot);

            //if the target is within roughly 45 degrees of the ai's forward vector and target is within a certain distance
            //controlled entity can see the target
            return (dot > 0 && dot > 0.5f) && (sightVector.getLength() < 400);
        }
        Log.d("hb::IsTargetVisible", "context doesn't contain info");
        return false;
    }
}
