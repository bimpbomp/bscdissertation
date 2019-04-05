package bham.student.txm683.framework.ai.behaviours.composites;

import android.util.Log;
import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.Status;

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

        resetQueue();

        Log.d("COMPNODE", "resetting...");

        //reset children status
        for (BNode child : children){
            child.reset(context);
        }
    }

    void resetQueue(){
        executionSequence.clear();
        executionSequence.addAll(children);
    }

    @Override
    public Status process(BContext context) {

        if (getStatus() != Status.RUNNING)
            reset(context);

        while(!executionSequence.isEmpty()){
            BNode child = executionSequence.peek();

            Status childStatus = child.process(context);
            Log.d("COMPNODE", "child status: " + childStatus);

            if (childStatus == continueExecutionStatus){
                executionSequence.poll();
                Log.d("COMPNODE", "polling child, queue size after: " + executionSequence.size());

            } else {
                setStatus(childStatus);
                Log.d("COMPNode", "child returned " + childStatus + " which is invalid. Stopping");
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