package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.RUNNING;

public abstract class CompositeBNode extends BNode {
    private List<BNode> children;
    private Status processedAllStatus;

    public CompositeBNode(Status processedAllStatus, BNode... children){
        if (children == null || children.length == 0) {
            throw new IllegalArgumentException("Children must not be null");
        }

        this.children = new ArrayList<>();
        this.children.addAll(Arrays.asList(children));

        this.processedAllStatus = processedAllStatus;
    }

    @Override
    public void init(BContext context) {

    }

    @Override
    public Status process(BContext context) {
        Status status;

        for (BNode child : getChildren()){

            if (child.getStatus() == RUNNING){
                //if the child is already running, process it again
                status = child.process(context);
            } else {
                //if child isn't running, init it and then process it
                child.init(context);
                status = child.process(context);
            }

            Log.d("hb::"+this.getClass().getSimpleName(), "child status: " + status);
            if (status != processedAllStatus){
                //if the child doesn't match the status value for execution to carry on, return status
                return status;
            }
        }
        //all children have finished processing, return the Status for this condition
        this.setStatus(processedAllStatus);
        return getStatus();
    }

    public List<BNode> getChildren() {
        return children;
    }
}
