package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.CONTROLLED_ENTITY;
import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.TARGET;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.*;

public class Tasks {

    private Tasks(){

    }

    public static BNode getRotateToTask(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(TARGET, CONTROLLED_ENTITY)){
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point target = (Point) context.getValue(TARGET);

                    Vector rotVector = new Vector(controlled.getCenter(), target).getUnitVector();

                    controlled.setRotationVector(rotVector);

                    return SUCCESS;
                }
                return FAILURE;
            }
        };
    }

    public static BNode getMoveTowardsPointTask(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, TARGET)){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point target = (Point) context.getValue(TARGET);

                    Vector movementVector = new Vector(controlled.getCenter(), target);

                    if (movementVector.getLength() < 10) {
                        controlled.setRequestedMovementVector(Vector.ZERO_VECTOR);
                        return SUCCESS;
                    } else {
                        controlled.setRequestedMovementVector(movementVector.getUnitVector());
                        return RUNNING;
                    }
                }
                return FAILURE;
            }
        };
    }
}
