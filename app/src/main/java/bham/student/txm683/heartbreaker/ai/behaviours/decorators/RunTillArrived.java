package bham.student.txm683.heartbreaker.ai.behaviours.decorators;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

public class RunTillArrived extends BNode {

    private BNode child;

    public RunTillArrived(BNode child) {
        this.child = child;
    }

    @Override
    public void reset(BContext context) {
        super.reset(context);
        child.setStatus(Status.READY);
    }

    @Override
    public Status process(BContext context) {

        Status childStatus = child.process(context);

        if (childStatus != Status.FAILURE){
            if (!context.containsVariables("arrived"))
                context.addVariable("arrived", false);

            boolean arrived = (boolean) context.getVariable("arrived");

            Log.d("PROCESS", "ai arrived: " + arrived);
            if (arrived){
                setStatus(Status.SUCCESS);
                return Status.SUCCESS;
            } else {
                setStatus(Status.RUNNING);
                return Status.RUNNING;
            }
        }

        setStatus(Status.FAILURE);
        return Status.FAILURE;
    }
}
