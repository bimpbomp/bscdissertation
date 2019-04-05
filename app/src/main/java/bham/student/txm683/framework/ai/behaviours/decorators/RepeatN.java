package bham.student.txm683.framework.ai.behaviours.decorators;

import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.Status;

public class RepeatN extends BNode {
    private BNode child;
    private int count;
    private int remaining;

    public RepeatN(int count, BNode child) {
        this.child = child;

        this.count = count;
        this.remaining = 0;
    }

    @Override
    public void reset(BContext context) {
        super.reset(context);
        child.setStatus(Status.READY);
        remaining = count;
    }

    @Override
    public Status process(BContext context) {

        if (getStatus() != Status.RUNNING){
            remaining = count;
        }

        if (remaining > 0){
            remaining--;
            child.process(context);

            setStatus(Status.RUNNING);
            return Status.RUNNING;
        }

        setStatus(Status.SUCCESS);
        return Status.SUCCESS;

    }
}
