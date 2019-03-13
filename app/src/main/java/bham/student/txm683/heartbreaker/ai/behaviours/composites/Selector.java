package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.FAILURE;

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
