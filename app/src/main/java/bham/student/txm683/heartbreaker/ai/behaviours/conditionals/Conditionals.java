package bham.student.txm683.heartbreaker.ai.behaviours.conditionals;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

public class Conditionals {

    private static Condition healthAboveThresholdCondition = context -> {
            if (context.containsKeys(CONTROLLED_ENTITY)){
                AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                int health = controlled.getHealth();
                int bound = (int) context.variableOrDefault("flee_health", controlled.getInitialHealth()/4);

                return health > bound;
            }
            return false;
        };

    private static Condition healthBelowThresholdCondition = context -> !healthAboveThresholdCondition.eval(context);

    private static Condition canSeePlayerCondition = (context -> {
        if (context.containsKeys(SIGHT_BLOCKED, SIGHT_VECTOR, CONTROLLED_ENTITY, FRIENDLY_BLOCKING_SIGHT)){
            //if the entity isn't blocked by a friendly or by a wall/door
            if (!(Boolean) context.getValue(SIGHT_BLOCKED) && !(Boolean) context.getValue(FRIENDLY_BLOCKING_SIGHT)) {
                boolean isOnScreen = ((AIEntity) context.getValue(CONTROLLED_ENTITY)).isOnScreen();

                Log.d("CANSEEPLAYER", isOnScreen+"");

                return isOnScreen;
            }
        }
        Log.d("CANSEEPLAYER", ""+false);
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

    private static Condition notAtDestinationCondition = context -> {
        if (context.containsVariables("arrived")){
            return !((boolean) context.getVariable("arrived"));
        }
        return false;
    };

    private static Condition notInCooldownCondition = context ->  !inCooldownCondition.eval(context);

    private Conditionals() {

    }

    public static ConditionalBNode arriving (BNode child){
        return new ConditionalBNode(child, context -> {
            if (context.containsVariables("arriving")){
                return (boolean) context.getVariable("arriving");
            }
            return false;
        });
    }

    public static ConditionalBNode notAtDestination (BNode child) {
        return new ConditionalBNode(child, notAtDestinationCondition);
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
