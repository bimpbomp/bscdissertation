package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import static bham.student.txm683.heartbreaker.ai.behaviours.BContext.LEVEL_STATE;

public class PathFindToLocation extends BNode {

    @Override
    public void init(BContext context) {

    }

    @Override
    public Status process(BContext context) {
        if (context.containsKey(LEVEL_STATE) && context.getValue(LEVEL_STATE) instanceof LevelState){

        }
        return Status.FAILURE;
    }
}
