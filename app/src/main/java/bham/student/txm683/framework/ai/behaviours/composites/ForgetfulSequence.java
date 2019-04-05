package bham.student.txm683.framework.ai.behaviours.composites;

import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.Status;

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
