package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.FAILURE;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.RUNNING;

public abstract class DecoratorBNode extends BNode {
    BNode child;

    public DecoratorBNode(BNode child){
        this.child = child;
    }

    abstract boolean condition(BContext context);

    @Override
    public void init(BContext context) {
        child.setStatus(FAILURE);
    }

    @Override
    public Status process(BContext context) {
        //if the condition for executing the child is true, execute the child
        if (condition(context)){

            Log.d("hb::" + this.getClass().getSimpleName(), "condition true");
            if (child.getStatus() != RUNNING){
                //if child isn't running, init it
                child.init(context);
            }
            return child.process(context);
        }
        Log.d("hb::" + this.getClass().getSimpleName(), "condition false");
        return FAILURE;
    }
}
