package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

/**
 * Returns SUCCESS unless the child fails
 */
public class RunningSuccessor extends BNode {
    private BNode child;

    public RunningSuccessor(BNode child) {
        this.child = child;
    }

    @Override
    public void reset(BContext context) {
        super.reset(context);
        child.setStatus(Status.READY);
    }

    @Override
    public Status process(BContext context) {

        Status childStatus = child.process(context);

        if (childStatus == Status.FAILURE){
            setStatus(Status.FAILURE);
            return Status.FAILURE;
        }

        setStatus(Status.SUCCESS);
        return Status.SUCCESS;
    }
}
