package bham.student.txm683.framework.ai.behaviours.composites;

import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.Status;

import static bham.student.txm683.framework.ai.behaviours.Status.FAILURE;

public class Selector extends CompositeBNode {
    public Selector(BNode... children) {
        super(FAILURE, children);
    }

    @Override
    public Status process(BContext context) {
        resetQueue();
        return super.process(context);
    }
}
