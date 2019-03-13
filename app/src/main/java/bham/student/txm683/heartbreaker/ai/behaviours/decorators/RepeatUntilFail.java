package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.*;

public class RepeatUntilFail extends BNode {
    private BNode child;

    public RepeatUntilFail(BNode child){
        this.child = child;
    }

    @Override
    public void reset(BContext context) {
        child.setStatus(READY);
    }

    @Override
    public Status process(BContext context) {
        
        Status childStatus = child.process(context);
        if (childStatus == FAILURE)
            return SUCCESS;

        return RUNNING;
    }
}

