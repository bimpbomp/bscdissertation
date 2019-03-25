package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.ai.behaviours.composites.ForgetfulSequence;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RepeatN;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RunTillArrived;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.Succeeder;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;

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

    private static BNode shootBehaviour(){
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

    private static BNode travelTo(boolean returnIncompletePath){
        return new Sequence(
                Tasks.plotPath(returnIncompletePath),
                new RunTillArrived(
                        new ForgetfulSequence(
                                Tasks.followPath(),
                                Tasks.courseCorrect(),
                                new Succeeder(
                                        Conditionals.arriving(
                                                Tasks.arrival()
                                        )
                                ),
                                Tasks.applyMovementForces()
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
                //Tasks.courseCorrect(),
                Tasks.applyMovementForces()
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

    public static BNode selfDestructTree(int fuse){
        BNode explode = explodeTree(fuse);
        return new Selector(
                Conditionals.canSeePlayer(
                        new Sequence(
                                driveAtPlayer(),
                                explode
                        )
                ),
                new ForgetfulSequence(
                        new Succeeder(
                                Conditionals.fuseStarted(
                                        explode
                                )
                        ),
                        Tasks.findMeshAdjacentToPlayer(),
                        travelTo(false)
                )
        );
    }

    public static BNode explodeTree(int fuse){
        return new Succeeder(new Sequence(
                        Tasks.flashRed(fuse),
                        Tasks.detonate()
                )
        );
    }

    private static BNode chaseTree(){

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

    private static BNode brokenDownTree(){
        return Conditionals.healthBelowThreshold(
                new Succeeder(
                        new Sequence(
                                Tasks.brokenDown(),
                                shootBehaviour(),
                                Tasks.doNothing(50)
                        )
                )
        );
    }

    public static BNode droneTree(){
        return new Selector(
                brokenDownTree(),
                chaseTree()
        );
    }

    public static BNode turretTree(){
        return new Selector(
                brokenDownTree(),
                new Selector(
                        Conditionals.canSeePlayer(
                                delayedShoot(50, 10)
                        ),
                        new Sequence(
                                Tasks.findMeshAdjacentToPlayer(),
                                travelTo(false)
                        )

                )
        );
    }

    public static BNode destructDroneTree(){
        return new Selector (
                Conditionals.healthBelowThreshold(
                        selfDestructTree(100)
                ),
                chaseTree()
        );
    }

    public static BNode healerTree(){
        return new Selector(
                new Sequence(
                        Tasks.findAIToHeal(),
                        travelTo(true),
                        Tasks.healField(150)
                ),
                Tasks.doNothing(75)
        );
    }
}