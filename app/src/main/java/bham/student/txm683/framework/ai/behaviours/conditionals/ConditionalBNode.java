package bham.student.txm683.framework.ai.behaviours.conditionals;

import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.Status;

import static bham.student.txm683.framework.ai.behaviours.Status.FAILURE;
import static bham.student.txm683.framework.ai.behaviours.Status.RUNNING;

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

            if (child.getStatus() != RUNNING){
                //if child isn't running, reset it
                child.reset(context);
            }

            Status status = child.process(context);
            setStatus(status);
            return status;
        }
        setStatus(FAILURE);
        return FAILURE;
    }
}
