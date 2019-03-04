package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import java.util.*;

public abstract class CompositeBNode extends BNode {
    private List<BNode> children;

    private Status continueExecutionStatus;
    private Queue<BNode> executionSequence;

    public CompositeBNode(Status continueExecutionStatus, BNode... children){
        if (children == null || children.length == 0) {
            throw new IllegalArgumentException("Children must not be null");
        }

        this.children = new ArrayList<>();

        this.children.addAll(Arrays.asList(children));

        this.continueExecutionStatus = continueExecutionStatus;
        this.executionSequence = new LinkedList<>();
    }

    @Override
    public void reset(BContext context) {
        //reset own status
        super.reset(context);

        //reset children status
        executionSequence.clear();
        executionSequence.addAll(children);

        for (BNode child : children){
            child.reset(context);
        }
    }

    @Override
    public Status process(BContext context) {

        if (getStatus() != Status.RUNNING)
            reset(context);

        while(!executionSequence.isEmpty()){
            BNode child = executionSequence.peek();

            Status childStatus = child.process(context);

            if (childStatus == continueExecutionStatus){
                executionSequence.poll();
            } else {
                setStatus(childStatus);
                return getStatus();
            }
        }
        setStatus(continueExecutionStatus);
        return getStatus();
    }

    public List<BNode> getChildren() {
        return children;
    }
}


/*
package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import java.util.*;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.READY;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.RUNNING;

public abstract class CompositeBNode extends BNode {
    private List<BNode> children;

    private Status continueExecutionStatus;

    public CompositeBNode(Status continueExecutionStatus, BNode... children){
        if (children == null || children.length == 0) {
            throw new IllegalArgumentException("Children must not be null");
        }


        this.children = new ArrayList<>();

        Arrays.asList(children).forEach((a) -> {
            this.children.add(a);

        });

        this.continueExecutionStatus = continueExecutionStatus;
    }

    @Override
    public void reset(BContext context) {
        //reset children status

        for (BNode child : children){
            child.setStatus(READY);
        }
    }

    @Override
    public Status process(BContext context) {
        Status status;

        for (BNode child : getChildren()){
            if (child.getStatus() == RUNNING){
                status = child.process(context);
            }
        }

        for (BNode child : getChildren()){

            if (child.getStatus() == RUNNING){
                //if the child is already running, process it again
                status = child.process(context);
            } else {
                //if child isn't running, reset it and then process it
                child.reset(context);
                status = child.process(context);
            }

            Log.d("hb::"+this.getClass().getSimpleName(), "child status: " + status);
            if (status != continueExecutionStatus){
                //if the child doesn't match the status value for execution to carry on, return status
                return status;
            }
        }
        //all children have finished processing, return the Status for this condition
        this.setStatus(continueExecutionStatus);
        return getStatus();
    }

    public List<BNode> getChildren() {
        return children;
    }
}
*/
