package bham.student.txm683.framework.ai.behaviours.conditionals;

import bham.student.txm683.framework.ai.behaviours.BContext;

public interface Condition {
    boolean eval(BContext context);
}
