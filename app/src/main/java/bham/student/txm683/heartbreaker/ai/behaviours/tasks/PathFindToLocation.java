package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.LEVEL_STATE;

public class PathFindToLocation extends BNode {

    @Override
    public void reset(BContext context) {

    }

    @Override
    public Status process(BContext context) {
        if (context.containsKeys(LEVEL_STATE)){

        }
        return Status.FAILURE;
    }
}
