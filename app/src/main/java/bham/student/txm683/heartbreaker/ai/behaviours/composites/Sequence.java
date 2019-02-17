package bham.student.txm683.heartbreaker.ai.behaviours.composites;

import bham.student.txm683.heartbreaker.ai.behaviours.BNode;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.SUCCESS;

public class Sequence extends CompositeBNode {

    public Sequence(BNode... children) {
        super(SUCCESS, children);
    }
}
