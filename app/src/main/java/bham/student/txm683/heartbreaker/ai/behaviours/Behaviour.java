package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.ForgetfulSequence;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.MemSelector;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RepeatUntilFail;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

public class Behaviour {

    private Behaviour(){

    }

    public static BNode idleBehaviour(){
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
    }

    public static BNode walkToRandomMeshBehaviour(){
        BNode randomMesh = new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(LEVEL_STATE)){
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);

                    List<Integer> keySet = new ArrayList<>(levelState.getRootMeshPolygons().keySet());

                    Random r = new Random();

                    context.addPair(MOVE_TO, levelState.getRootMeshPolygons().get(keySet.get(r.nextInt(keySet.size()))).getCenter());

                    return Status.SUCCESS;
                }
                return Status.FAILURE;
            }
        };

        return new Sequence(
                randomMesh,
                Tasks.plotPath(),
                followPathBehaviour(),
                Tasks.courseCorrect()
        );
    }

    public static BNode turretIdleBehaviour(){
        return new Sequence(
                Tasks.randomPointInMesh(),
                Tasks.idleRotDamp(),
                Tasks.rotateToMoveTo(),
                Tasks.doNothing()
        );
    }

    public static BNode walkToPointBehaviour(){
        return new Sequence(
                Tasks.rotateToMoveTo(),
                Tasks.moveTowardsPoint()
        );
    }

    public static BNode followPathBehaviour(){
        return new Sequence(
                Tasks.plotPath(),
                Tasks.followPath()
        );
    }

    public static BNode stationaryShootBehaviour(){
        return Conditionals.canSeePlayer(
                new ForgetfulSequence(
                        Tasks.attackRotDamp(),
                        Tasks.aim(),
                        Tasks.rotateToTarget(),
                        Conditionals.notInCooldown(
                                Tasks.shoot()
                        )
                )
        );
    }

    public static BNode fleeToCore(){
        return new Sequence(
                Tasks.setMoveToAsCore(),
                followPathBehaviour(),
                new BNode() {
                    @Override
                    public Status process(BContext context) {
                        if (context.containsKeys(PATH, CONTROLLED_ENTITY)){
                            AIEntity entity  = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                            List<Point> path = ((PathWrapper) context.getValue(PATH)).path();

                            if (path.size() == 0)
                                return Status.SUCCESS;

                            Point goal = path.get(path.size()-1);

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
    }

    public static BNode turretTree(){
        BNode turretAttack = new Selector(
                Conditionals.inCooldown(
                        new Sequence(
                                Tasks.attackRotDamp(),
                                Tasks.aim(),
                                Tasks.rotateToTarget()
                        )
                ),
                Behaviour.stationaryShootBehaviour()
        );

        return new Selector(
                Conditionals.canSeePlayer(
                        turretAttack
                ),
                Behaviour.turretIdleBehaviour()
        );
    }

    public static BNode fleeToAlly(){
        return new Sequence(
                Tasks.plotPathToAnAI(),
                Tasks.followPath()
        );
    }

    public static BNode droneTree(){
        BNode flee = new Sequence(
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
                    ));
        return walkToRandomMeshBehaviour();

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