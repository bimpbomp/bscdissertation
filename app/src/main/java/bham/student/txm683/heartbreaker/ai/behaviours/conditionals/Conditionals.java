package bham.student.txm683.heartbreaker.ai.behaviours.conditionals;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

public class Conditionals {

    private static Condition healthAboveThresholdCondition = context -> {
            if (context.containsKeys(CONTROLLED_ENTITY, HEALTH_BOUND)){
                int health = ((AIEntity) context.getValue(CONTROLLED_ENTITY)).getHealth();
                int bound = (int) context.getValue(HEALTH_BOUND);

                return health > bound;
            }
            return false;
        };

    private static Condition healthBelowThresholdCondition = context -> !healthAboveThresholdCondition.eval(context);

    private static Condition canSeePlayerCondition = (context -> {
        if (context.containsKeys(SIGHT_BLOCKED, SIGHT_VECTOR, CONTROLLED_ENTITY, FRIENDLY_BLOCKING_SIGHT)){
            if ((Boolean) context.getValue(SIGHT_BLOCKED)){
                ((AIEntity) context.getValue(CONTROLLED_ENTITY)).revertToDefaultColor();
            } else if ((Boolean) context.getValue(FRIENDLY_BLOCKING_SIGHT)) {
                ((AIEntity) context.getValue(CONTROLLED_ENTITY)).setColor(Color.YELLOW);
            } else {
                //((AIEntity) context.getValue(CONTROLLED_ENTITY)).applyRotationalForces((Vector) context.getValue(SIGHT_VECTOR));
                ((AIEntity) context.getValue(CONTROLLED_ENTITY)).setColor(Color.BLACK);

                return true;
            }
        }
        return false;
    });

    private static Condition canNotSeePlayerCondition = context -> !canSeePlayerCondition.eval(context);

    private static Condition inCooldownCondition = context -> {
        if (context.containsKeys(CONTROLLED_ENTITY)){
            AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

            controlled.getWeapon().tickCooldown();

            return controlled.getWeapon().inCooldown();
        }
        return false;
    };

    private static Condition notInCooldownCondition = context ->  !inCooldownCondition.eval(context);

    private Conditionals() {

    }

    public static ConditionalBNode inCooldown(BNode child){
        return new ConditionalBNode(child, inCooldownCondition);
    }

    public static ConditionalBNode notInCooldown(BNode child){
        return new ConditionalBNode(child, notInCooldownCondition);
    }

    public static ConditionalBNode healthBelowThreshold(BNode child){
        return new ConditionalBNode(child, healthBelowThresholdCondition);
    }

    public static ConditionalBNode healthAboveThreshold(BNode child){
        return new ConditionalBNode(child, healthAboveThresholdCondition);
    }

    public static ConditionalBNode canSeePlayer(BNode child){
        return new ConditionalBNode(child, canSeePlayerCondition);
    }

    public static ConditionalBNode canNotSeePlayer(BNode child){
        return new ConditionalBNode(child, canNotSeePlayerCondition);
    }
}
