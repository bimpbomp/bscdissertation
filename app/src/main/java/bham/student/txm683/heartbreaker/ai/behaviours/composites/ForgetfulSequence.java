package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

public class ForgetfulSequence extends Sequence {

    public ForgetfulSequence(BNode... children) {
        super(children);
    }

    @Override
    public Status process(BContext context) {
        resetQueue();
        return super.process(context);
    }
}
