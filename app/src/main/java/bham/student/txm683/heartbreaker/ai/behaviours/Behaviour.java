package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;

public class Behaviour {

    private Behaviour(){

    }

    public static BNode idleBehaviour(){
        return new Sequence(
            Tasks.randomPointInMesh(),
            walkToPointBehaviour(),
            Tasks.doNothing()
        );
    }

    public static BNode turretIdleBehaviour(){
        return new Sequence(
                Tasks.randomPointInMesh(),
                Tasks.idleRotDamp(),
                Tasks.rotateToTarget(),
                Tasks.doNothing()
        );
    }

    public static BNode walkToPointBehaviour(){
        return new Sequence(
                Tasks.rotateToTarget(),
                Tasks.moveTowardsTarget()
        );
    }

    public static BNode followPathBehaviour(){
        return new Sequence(
                Tasks.plotPath(),
                Tasks.followPath()
        );
    }

    public static BNode stationaryShootBehaviour(){
        return new Sequence(
                Tasks.checkLineOfSight(),
                Tasks.attackRotDamp(),
                Tasks.aim(),
                Tasks.rotateToTarget(),
                Tasks.shoot()
        );
    }
}