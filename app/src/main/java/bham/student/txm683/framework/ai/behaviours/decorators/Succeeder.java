package bham.student.txm683.framework.ai.behaviours.decorators;

import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.Status;

public class Succeeder extends BNode {
    private BNode child;

    public Succeeder(BNode child) {
        this.child = child;
    }

    @Override
    public void reset(BContext context) {
        super.reset(context);
        child.reset(context);
    }

    @Override
    public Status process(BContext context) {

        child.process(context);

        setStatus(Status.SUCCESS);
        return Status.SUCCESS;
    }
}
