package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.ai.behaviours.composites.ForgetfulSequence;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.NotAtDestination;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.Succeeder;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;

public class Behaviour {

    private Behaviour(){

    }

    /*public static BNode idleBehaviour(){
        return new Sequence(
            walkToRandomPointBehaviour(),
            Tasks.doNothing()
        );
    }

    public static BNode walkToRandomPointBehaviour(){
        return new Sequence(
                Tasks.randomPointInMesh(),
                walkToPointBehaviour()
        );
    }*/

    public static BNode walkToRandomMeshBehaviour(){

        return new Sequence(
                Tasks.patrol(),
                Tasks.plotPath(),
                Tasks.followPath(),
                //Tasks.seek(),
                Tasks.courseCorrect()
        );
    }

    public static BNode followPathBehaviour(){

        return new NotAtDestination(
                new Sequence(
                        Tasks.followPath(),
                        Tasks.courseCorrect()
                )
        );
    }

    public static BNode stationaryShootBehaviour(){
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
                new NotAtDestination(
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
                                Tasks.aim(),
                                Tasks.rotateToTarget(),
                                Tasks.doNothing(30),
                                Tasks.shoot()
                        )
                )
        );
    }

    /*public static BNode fleeToCore(){
        return new Sequence(
                Tasks.setMoveToAsCore(),
                followPathBehaviour(),
                new BNode() {
                    @Override
                    public Status process(BContext context) {
                        if (context.containsKeys(PATH, CONTROLLED_ENTITY)){
                            AIEntity entity  = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                            List<Point> basePath = ((PathWrapper) context.getValue(PATH)).basePath();

                            if (basePath.size() == 0)
                                return Status.SUCCESS;

                            Point goal = basePath.get(basePath.size()-1);

                            Vector v = new Vector (entity.getCenter(), goal);

                            if (v.getLength() < 200)
                                return Status.SUCCESS;

                            entity.setRequestedMovementVector(v.getUnitVector());
                            entity.setRotationVector(v.getUnitVector());
                            return Status.RUNNING;
                        }
                        return Status.FAILURE;
                    }
                },
                walkToRandomPointBehaviour()
        );
    }*/

    public static BNode fleeToAlly(){
        return new Sequence(
                Tasks.plotPathToAnAI(),
                Tasks.followPath()
        );
    }

    public static BNode droneTree(){
        return new Selector(
                new Sequence(
                        Tasks.patrol(),
                        travelTo()/*,
                        Tasks.doNothing()*/
                )
        );

        /*BNode flee = new Sequence(
                new MemSelector(
                        fleeToAlly(),
                        fleeToCore()
                ),
                new RepeatUntilFail(
                        Conditionals.canNotSeePlayer(
                                Behaviour.idleBehaviour()
                        )
                )
        );

        BNode droneAttack = new Sequence(
                    new Selector(
                            Conditionals.inCooldown(
                                    new Sequence(
                                            Tasks.randomPointInMesh(),
                                            Tasks.moveTowardsPoint()
                                    )
                            ),
                            stationaryShootBehaviour()
                    ));*/



        /*return new Selector(
                Conditionals.canSeePlayer(
                    new Selector(
                            Conditionals.healthAboveThreshold(
                                    droneAttack
                            ),
                            flee
                    )
                ),
                Conditionals.healthBelowThreshold(
                        flee
                ),

                Behaviour.idleBehaviour()
        );*/
    }
}