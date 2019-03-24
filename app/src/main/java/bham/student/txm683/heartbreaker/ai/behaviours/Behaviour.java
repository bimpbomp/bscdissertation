package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.ai.behaviours.composites.ForgetfulSequence;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RepeatN;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RepeatUntilFail;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RunTillArrived;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.Succeeder;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;

public class Behaviour {

    private Behaviour(){

    }

    public static BNode flee(){
        return new Sequence(
                Tasks.findAI(),
                travelTo()
        );
    }

    public static BNode walkToRandomMeshBehaviour(){

        return new Sequence(
                Tasks.patrol(),
                Tasks.plotPath(),
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

    public static BNode travelTo(){
        return new Sequence(
                Tasks.plotPath(),
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

    public static BNode turretTree(){
        return new Sequence(
                Tasks.pickRandomMesh(),
                Tasks.plotPath(),
                travelTo(),
                Conditionals.canSeePlayer(
                        new Sequence(
                                new RepeatN(50,
                                        new Sequence(
                                                Tasks.aim(),
                                                Tasks.rotateToTarget()
                                        )
                                ),
                                Tasks.shoot(),
                                Tasks.doNothing(25)
                        )
                )
        );
    }

    public static BNode healerTree(){
        return new Selector(
                new Sequence(
                       Tasks.findAIToHeal(),
                       travelTo(),
                       Tasks.healField()
                ),
                Tasks.patrol(),
                travelTo()
        );
    }

    public static BNode driveAtPlayer(){
        return new Sequence(
                Tasks.setHeadingAsPlayer(),
                Tasks.seek(),
                Tasks.courseCorrect()
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

    public static BNode selfDestructTree(){
        return new RepeatUntilFail(
                new Selector(
                        Conditionals.canSeePlayer(
                                new Sequence(
                                        driveAtPlayer(),
                                        explodeTree()
                                )
                        ),
                        Tasks.plotPathToMeshAdjacentToPlayer(),
                        travelTo()
                )
        );
    }

    public static BNode explodeTree(){
        return new Sequence(
                Tasks.flashRed(75),
                Tasks.detonate()
        );
    }

    public static BNode chaseTree(){

        return new Selector(
                Conditionals.healthBelowThreshold(
                        new Succeeder(
                                flee()
                        )
                ),
                new Sequence(
                        Tasks.plotPathToMeshAdjacentToPlayer(),
                        new RunTillArrived(
                                new ForgetfulSequence(
                                        new Succeeder(
                                                Conditionals.canSeePlayer(
                                                        shootBehaviour()
                                                )
                                        ),
                                        travelTo()
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
                )
        );
    }

    public static BNode droneTree(){
        return new Selector(
                new Sequence(
                        Tasks.patrol(),
                        travelTo()
                )
        );
    }
}