package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;

public class MemSelector extends CompositeBNode {

    public MemSelector(BNode... children) {
        super(Status.FAILURE, children);
    }
}
