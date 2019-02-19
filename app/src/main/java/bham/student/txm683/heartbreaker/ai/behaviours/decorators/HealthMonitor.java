package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;

public class HealthMonitor extends DecoratorBNode {

    public HealthMonitor(BNode child) {
        super(child);
    }

    @Override
    boolean condition(BContext context) {
        if (context.containsKey(BContext.HEALTH_BOUND) && context.containsKey(BContext.HOST_ENTITY)){
            Log.d("hb::HealthMonitor", "context has info");
            try {
                //return true if the controlled entity's health is below the bound.
                Log.d("hb::HealthMonitor", (((AIEntity) context.getValue(BContext.HOST_ENTITY)).getHealth() <
                        ((int) context.getValue(BContext.HEALTH_BOUND)))+"");
                return ((AIEntity) context.getValue(BContext.HOST_ENTITY)).getHealth() <
                        ((int) context.getValue(BContext.HEALTH_BOUND));
            } catch (Exception e){
                //if a problem occurs processing the values, return false
                Log.d("hb::HealthMonitor", "exception occured");
            }

        }
        Log.d("hb::HealthMonitor", "context doesnt info");
        return false;
    }
}
