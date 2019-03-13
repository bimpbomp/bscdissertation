package bham.student.txm683.heartbreaker.ai.behaviours.conditionals;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.FAILURE;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.RUNNING;

public class ConditionalBNode extends BNode {
    private BNode child;
    private Condition condition;

    public ConditionalBNode(BNode child, Condition condition){
        this.child = child;
        this.condition = condition;
    }

    @Override
    public void reset(BContext context) {
        child.setStatus(FAILURE);
    }

    @Override
    public Status process(BContext context) {
        //if the eval for executing the child is true, execute the child
        if (condition.eval(context)){

            Log.d("hb::" + this.getClass().getSimpleName(), "eval true");
            if (child.getStatus() != RUNNING){
                //if child isn't running, reset it
                child.reset(context);
            }
            return child.process(context);
        }
        Log.d("hb::" + this.getClass().getSimpleName(), "eval false");
        return FAILURE;
    }
}
