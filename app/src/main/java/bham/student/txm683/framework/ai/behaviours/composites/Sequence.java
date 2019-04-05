package bham.student.txm683.framework.ai.behaviours.composites;

import bham.student.txm683.framework.ai.behaviours.BNode;

import static bham.student.txm683.framework.ai.behaviours.Status.SUCCESS;

public class Sequence extends CompositeBNode {

    public Sequence(BNode... children) {
        super(SUCCESS, children);
    }
}
