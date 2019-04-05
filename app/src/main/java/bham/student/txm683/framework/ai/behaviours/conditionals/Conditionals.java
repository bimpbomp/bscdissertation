package bham.student.txm683.framework.ai.behaviours.conditionals;

import android.util.Log;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.AIEntity;

import static bham.student.txm683.framework.ai.behaviours.BKeyType.*;

public class Conditionals {

    //returns true if the controlled entity's health is below the threshold in the context
    private static Condition healthAboveThresholdCondition = context -> {
            if (context.containsCompulsory(CONTROLLED_ENTITY)){
                AIEntity controlled = (AIEntity) context.getCompulsory(CONTROLLED_ENTITY);

                int health = controlled.getHealth();
                int bound = (int) context.variableOrDefault("flee_health", controlled.getInitialHealth()/4);

                return health > bound;
            }
            return false;
        };

    private static Condition healthBelowThresholdCondition = context -> !healthAboveThresholdCondition.eval(context);

    //returns true if the SIGHT_BLOCKED property in the context is true
    private static Condition canSeePlayerCondition = (context -> {
        if (context.containsCompulsory(SIGHT_BLOCKED, SIGHT_VECTOR, CONTROLLED_ENTITY, FRIENDLY_BLOCKING_SIGHT)){
            //if the entity isn't blocked by a friendly or by a wall/door
            if (!(Boolean) context.getCompulsory(SIGHT_BLOCKED) && !(Boolean) context.getCompulsory(FRIENDLY_BLOCKING_SIGHT)) {
                boolean isOnScreen = ((AIEntity) context.getCompulsory(CONTROLLED_ENTITY)).isOnScreen();

                Log.d("CANSEEPLAYER", isOnScreen+"");

                return isOnScreen;
            }
        }
        Log.d("CANSEEPLAYER", ""+false);
        return false;
    });

    //returns true if the entity's weapon is in cooldown
    private static Condition inCooldownCondition = context -> {
        if (context.containsCompulsory(CONTROLLED_ENTITY)){
            AIEntity controlled = (AIEntity) context.getCompulsory(CONTROLLED_ENTITY);

            controlled.getWeapon().tickCooldown();

            return controlled.getWeapon().inCooldown();
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

    public static ConditionalBNode fuseStarted (BNode child){
        return new ConditionalBNode(child, context -> {
            Log.d("TASK", context.containsVariables("fuse_started")+"");
            return context.containsVariables("fuse_started");

        });
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
}
