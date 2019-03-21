package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

public class RepeatN extends BNode {
    private BNode child;
    private int count;
    private int remaining;

    public RepeatN(int count, BNode child) {
        this.child = child;
    }

    @Override
    public void reset(BContext context) {
        super.reset(context);
        child.setStatus(Status.READY);
        remaining = count;
    }

    @Override
    public Status process(BContext context) {
        //TODO implement
        return null;
    }
}
