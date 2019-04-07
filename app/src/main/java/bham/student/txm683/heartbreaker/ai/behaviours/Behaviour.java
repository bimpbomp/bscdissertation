package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.composites.ForgetfulSequence;
import bham.student.txm683.framework.ai.behaviours.composites.Selector;
import bham.student.txm683.framework.ai.behaviours.composites.Sequence;
import bham.student.txm683.framework.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.framework.ai.behaviours.decorators.RepeatN;
import bham.student.txm683.framework.ai.behaviours.decorators.Succeeder;
import bham.student.txm683.framework.ai.behaviours.tasks.Tasks;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RunTillArrived;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.TankTasks;

public class Behaviour {

    private Behaviour(){

    }

    public static BNode walkToRandomMeshBehaviour(){

        return new Sequence(
                Tasks.pickRandomMesh(),
                Tasks.plotPath(false),
                Tasks.followPath(),
                Tasks.courseCorrect()
        );
    }

    public static BNode shootBehaviour(){
        return Conditionals.canSeePlayer(
                new ForgetfulSequence(
                        Tasks.aim(),
                        Tasks.rotateToTarget(),
                        Conditionals.notInCooldown(
                                Tasks.shoot()
                        )
                )
        );
    }

    public static BNode travelTo(boolean returnIncompletePath){
        return new Sequence(
                Tasks.plotPath(returnIncompletePath),
                new RunTillArrived(
                        new ForgetfulSequence(
                                Tasks.followPath(),
                                //Tasks.courseCorrect(),
                                new Succeeder(
                                        Conditionals.arriving(
                                                Tasks.arrival()
                                        )
                                ),
                                TankTasks.applyMovementForces()
                        )
                )
        );
    }

    public static BNode delayedShoot(int delayInTicks, int idleAfter){
        return new Sequence(
                new RepeatN(delayInTicks,
                        new Sequence(
                                Tasks.aim(),
                                Tasks.rotateToTarget()
                        )
                ),
                Tasks.shoot(),
                Tasks.doNothing(idleAfter)
        );
    }

    public static BNode driveAtPlayer(){
        return new Sequence(
                Tasks.setHeadingAsPlayer(),
                Tasks.seek(),
                TankTasks.applyMovementForces()
        );
    }

    public static BNode engageTree(){
        return Conditionals.canSeePlayer(
                new Sequence(
                        driveAtPlayer(),
                        shootBehaviour()
                )
        );
    }

    public static BNode explodeTree(int fuse){
        return new Succeeder(new Sequence(
                        TankTasks.flashRed(fuse),
                        TankTasks.detonate()
                )
        );
    }

    public static BNode chaseTree(){

        return new Sequence(
                Tasks.findMeshAdjacentToPlayer(),
                new RunTillArrived(
                        new ForgetfulSequence(
                                new Succeeder(
                                        Conditionals.canSeePlayer(
                                                shootBehaviour()
                                        )
                                ),
                                travelTo(false)
                        )
                ),
                new Selector(
                        Conditionals.canSeePlayer(
                                new RepeatN(2,
                                        new Sequence(
                                                new Succeeder(
                                                        shootBehaviour()
                                                ),
                                                Tasks.doNothing(25)
                                        )
                                )
                        )
                )
        );
    }


}