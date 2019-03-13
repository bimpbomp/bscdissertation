package bham.student.txm683.heartbreaker.ai.behaviours.conditionals;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;

public interface Condition {
    boolean eval(BContext context);
}
