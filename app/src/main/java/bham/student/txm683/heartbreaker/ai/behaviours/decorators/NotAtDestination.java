package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

public class NotAtDestination extends BNode {

    private BNode child;

    public NotAtDestination(BNode child) {
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

        if (childStatus == Status.RUNNING){
            child.reset(context);
            setStatus(Status.RUNNING);
            return Status.RUNNING;
        }

        setStatus(childStatus);
        return childStatus;
    }
}
