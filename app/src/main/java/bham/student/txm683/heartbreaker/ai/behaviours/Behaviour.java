package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;

public class Behaviour {

    private Behaviour(){

    }

    public static BNode getIdleTree(){
        return new Sequence(

        );
    }

    public static BNode getWalkToTree(){
        return new Sequence(
                Tasks.getRotateToTask(),
                Tasks.getMoveTowardsPointTask()
        );
    }
}
