package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.SIGHT_BLOCKED;

public class IsTargetVisible extends DecoratorBNode {

    public IsTargetVisible(BNode child) {
        super(child);
    }

    @Override
    boolean condition(BContext context) {
        /*if (context.containsKeys(BContext.ATTACK_TARGET) && context.containsKeys(BContext.CONTROLLED_ENTITY)){
            Log.d("hb::IsTargetVisible", "Context contains needed info");


            Entity target = (Entity) context.getValue(BContext.ATTACK_TARGET);
            AIEntity controlledEntity = (AIEntity) context.getValue(BContext.CONTROLLED_ENTITY);

            Vector sightVector = new Vector (controlledEntity.getCenter(), target.getCenter());

            float dot = sightVector.getUnitVector().dot(controlledEntity.getForwardUnitVector());

            context.addPair("sight_vector", sightVector);

            Log.d("hb::IsTargetVisible", "sight vector length: " + sightVector.getLength());
            Log.d("hb::IsTargetVisible", "dot product: " + dot);

            //if the target is within roughly 45 degrees of the ai's forward vector and target is within a certain distance
            //controlled entity can see the target
            return (dot > 0 && dot > 0.5f) && (sightVector.getLength() < 400);
        }*/
        if (context.containsKeys(SIGHT_BLOCKED) && context.getValue(SIGHT_BLOCKED) instanceof Boolean){
            boolean visible = !((boolean) context.getValue(SIGHT_BLOCKED));

            Log.d("hb::IsTargetVisible", "contextvalue: " + ((boolean) context.getValue(SIGHT_BLOCKED)));

            return visible;
        }
        Log.d("hb::IsTargetVisible", "context doesn't contain info");
        return false;
    }
}
