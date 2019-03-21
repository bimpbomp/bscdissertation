package bham.student.txm683.heartbreaker.ai.behaviours;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.ForgetfulSequence;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Sequence;
import bham.student.txm683.heartbreaker.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.NotAtDestination;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.RunningSuccessor;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

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
        BNode patrol = new BNode() {

            List<Integer> patrolPath;

            @Override
            public void construct() {
                super.construct();
                patrolPath = new ArrayList<>();
                patrolPath.addAll(Arrays.asList(4, 19, 55, 34, 56));
            }

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(LEVEL_STATE, CONTROLLED_ENTITY)){
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    if (!context.containsVariables("patrol")){
                        context.addVariable("patrol", 0);
                    }

                    int patrolIdx = (int) context.getVariable("patrol");

                    if (context.containsVariables("plottingFailed") && ((boolean) context.getVariable("plottingFailed"))){
                        patrolIdx++;
                        if (patrolIdx >= patrolPath.size())
                            patrolIdx = 0;
                    }

                    /*float smallestDistance = Float.MAX_VALUE;
                    int patrolIdx = 0;
                    for (int i = 0; i < patrolPath.size(); i++){
                        int id = patrolPath.get(i);

                        Point center = levelState.getRootMeshPolygons().get(id).getCenter();

                        float distance = new Vector(controlled.getCenter(), center).getLength();

                        if (smallestDistance > distance){
                            smallestDistance = distance;
                            patrolIdx = i;
                        }
                    }

                    patrolIdx++;
                    if (patrolIdx >= patrolPath.size())
                        patrolIdx = 0;*/

                    Point point = levelState.getRootMeshPolygons().get(patrolPath.get(patrolIdx)).getCenter();

                    float distance = new Vector(controlled.getCenter(), point).getLength();

                    if (distance < 70){
                        Log.d("AVOID", "arrived at: " + patrolPath.get(patrolIdx));
                        patrolIdx++;

                        if (patrolIdx >= patrolPath.size()){
                            patrolIdx = 0;
                        }
                    } else {
                        Log.d("AVOID", "not arrived to patrol point in mesh " + patrolPath.get(patrolIdx) + " yet, " +
                                "distance to go: " + distance);
                    }

                    context.addVariable("patrol", patrolIdx);

                    Log.d("AVOID", "heading to: " + patrolPath.get(patrolIdx));

                    context.addPair(MOVE_TO, point);

                    return Status.SUCCESS;
                }
                return Status.FAILURE;
            }
        };

        return new ForgetfulSequence(
                patrol,
                Tasks.plotPath(),
                new RunningSuccessor(Tasks.followPath()),
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

    public static BNode turretTree(){
        return new Sequence(
                Tasks.plotToRandomMesh(),
                Tasks.plotPath(),
                followPathBehaviour(),
                Conditionals.canSeePlayer(
                        new Sequence(
                                Tasks.aim(),
                                Tasks.rotateToTarget(),
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
    }*/

    public static BNode fleeToAlly(){
        return new Sequence(
                Tasks.plotPathToAnAI(),
                Tasks.followPath()
        );
    }

    public static BNode droneTree(){

        BNode shoot = stationaryShootBehaviour();
        return new Selector(
                new ForgetfulSequence(
                        walkToRandomMeshBehaviour(),
                        shoot
                ),
                shoot
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