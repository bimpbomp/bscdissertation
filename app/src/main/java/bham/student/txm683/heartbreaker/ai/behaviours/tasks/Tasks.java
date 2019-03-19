package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.utils.AStar;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.List;
import java.util.Random;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.*;

public class Tasks {

    private Tasks(){

    }



    public static BNode doNothing(){
        return new BNode() {
            @Override
            public void reset(BContext context) {
                super.reset(context);
                context.removePair(TIME_LEFT_IN_IDLE);
            }

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY)){
                    if (getStatus() == RUNNING){
                        Log.d("TASKS doNothing", "is already running");

                        if (context.containsKeys(TIME_LEFT_IN_IDLE)){
                            int timeLeftInIdle = (int) context.getValue(TIME_LEFT_IN_IDLE) - 1;
                            context.addPair(TIME_LEFT_IN_IDLE, timeLeftInIdle);

                            setMovementToZero(context);

                            if (timeLeftInIdle < 1){
                                Log.d("TASKS doNothing", "has succeeded");
                                setStatus(SUCCESS);
                            } else {
                                Log.d("TASKS doNothing", timeLeftInIdle + " time left");
                                setStatus(RUNNING);
                            }
                        }

                    } else {
                        Log.d("TASKS doNothing", "was not already running");
                        if (context.containsKeys(TIME_PER_IDLE)) {
                            Log.d("TASKS doNothing", "setting movement to zero");
                            int timePerIdle = (int) context.getValue(TIME_PER_IDLE);
                            context.addPair(TIME_LEFT_IN_IDLE, timePerIdle);

                            setMovementToZero(context);
                        }
                        setStatus(RUNNING);
                    }

                    return getStatus();
                }
                Log.d("TASKS", "doNothing has failed");
                setStatus(FAILURE);
                return FAILURE;
            }

            private void setMovementToZero(BContext context){
                AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                controlled.setRequestedMovementVector(Vector.ZERO_VECTOR);
                controlled.setRotationVector(Vector.ZERO_VECTOR);
            }
        };
    }

    public static BNode courseCorrect(){
        return new BNode() {

            @Override
            public Status process(BContext context) {


                if (context.containsKeys(CONTROLLED_ENTITY, LEVEL_STATE) && context.containsVariables("course_correct:heading")){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    Point heading = (Point) context.getVariable("course_correct:heading");

                    CollisionManager collisionManager = ((LevelState) context.getValue(LEVEL_STATE)).getCollisionManager();

                    Vector steeringAxis = collisionManager.getPathAroundObstacle(controlled, heading);

                    if (!steeringAxis.equals(Vector.ZERO_VECTOR)){
                        //correction needs to take place


                        controlled.addForce(steeringAxis.sMult(100));
                    } else {
                        Log.d("AVOID", "no obstacles in way");
                    }

                    /*List<Point> pathAroundObstacle = collisionManager.getPathAroundObstacle(controlled, heading);

                    if (pathAroundObstacle == null || pathAroundObstacle.size() == 0){
                        Log.d("AVOID", "no obstacles in way");
                        setStatus(SUCCESS);
                        return SUCCESS;
                    }

                    Point target = new Point();
                    for (Point point : pathAroundObstacle){
                        if (CollisionManager.euclideanHeuristic(controlled.getFront(), point) > 70){

                            target = point;
                            break;
                        }
                    }*/

                    /*Vector v = new Vector(controlled.getCenter(), target);

                    controlled.setRotationVector(v);
                    controlled.setRequestedMovementVector(v);*/

                    setStatus(RUNNING);
                    return RUNNING;
                }
                Log.d("AVOID", "FAILED");

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode plotPath(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, CURRENT_MESH, LEVEL_STATE)){
                    Log.d("TASK plotPath", "plotting...");
                    Log.d("TASK plotPath", "levelstate is null: " + (context.getValue(LEVEL_STATE) == null));

                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);
                    //context.addPair(MOVE_TO, levelState.getPlayer().getCenter());

                    AStar a = new AStar((AIEntity) context.getValue(CONTROLLED_ENTITY),
                            levelState.getRootMeshPolygons(),
                            levelState.getMeshGraph());

                    boolean plotted = a.plotPath();

                    if (plotted) {
                        setStatus(SUCCESS);
                        return SUCCESS;
                    }
                }
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode followPath(){
        return new BNode() {

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, PATH)){

                    int pointInPath = 0;

                    if (getStatus() == RUNNING){
                        try {
                            pointInPath = (int) context.getVariable("follow:point_in_path");

                        } catch (Exception e){
                            setStatus(FAILURE);
                            return FAILURE;
                        }
                    }

                    AIEntity aiEntity = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    List<Point> path = ((PathWrapper) context.getValue(PATH)).path();

                    StringBuilder stringBuilder = new StringBuilder();

                    for (Point point : path){
                        stringBuilder.append(point);
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append("END");

                    Log.d("TASK followPath", stringBuilder.toString());
                    Log.d("TASK followPath", "point in path: " + pointInPath);

                    if (pointInPath >= path.size()-1) {
                        Log.d("TASK followPath", "path completed!");
                        setStatus(SUCCESS);
                        return SUCCESS;
                    }

                    Point currentPoint = path.get(pointInPath);

                    if (new Vector(aiEntity.getCenter(), currentPoint).getLength() < 50){
                        pointInPath++;

                        if (pointInPath > path.size()-1) {
                            Log.d("TASK followPath", "path completed!!");
                            aiEntity.setRequestedMovementVector(Vector.ZERO_VECTOR);

                            setStatus(SUCCESS);
                            return SUCCESS;
                        }

                        currentPoint = path.get(pointInPath);
                    }

                    Vector closenessV = new Vector(aiEntity.getCenter(), currentPoint).getUnitVector();

                    Log.d("TASK followPath", "heading to: " + currentPoint + ", vector to point: " + closenessV.relativeToString());

                    context.addVariable("course_correct:heading", currentPoint);

                    aiEntity.addForce(closenessV.sMult(100));
                    aiEntity.setRotationVector(closenessV);

                    /*aiEntity.setRequestedMovementVector(closenessV);
                    aiEntity.setRotationVector(closenessV);*/

                    setStatus(SUCCESS);
                    return SUCCESS;

                }
                Log.d("TASK followPath", "task failed...");

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode randomPointInMesh(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsKeys(CURRENT_MESH, CONTROLLED_ENTITY)){
                    MeshPolygon currentMesh = (MeshPolygon) context.getValue(CURRENT_MESH);
                    AIEntity aiEntity = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    Point target = currentMesh.getRandomPointInMesh();

                    Vector v = new Vector(aiEntity.getCenter(), target);

                    if (v.getLength() > 400)
                        v = v.setLength(400);

                    Log.d("TASKS", "target: " + v.getHead());
                    context.addPair(MOVE_TO, v.getHead());

                    return SUCCESS;
                }
                Log.d("TASKS", "randomPointInMesh doesnt contains keys");
                return FAILURE;
            }
        };
    }

    public static BNode rotateToTarget(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                return rotate(context, TARGET);
            }
        };
    }

    public static BNode rotateToMoveTo(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                return rotate(context, MOVE_TO);
            }
        };
    }

    private static Status rotate(BContext context, BKeyType point){

        if (context.containsKeys(MOVE_TO, CONTROLLED_ENTITY)){

            Point target = (Point) context.getValue(point);

            Log.d("TASKS", "rotateTo contains keys");
            AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

            Vector rotVector = new Vector(controlled.getCenter(), target).getUnitVector();

            float angle = Vector.calculateAngleBetweenVectors(controlled.getForwardUnitVector(), rotVector);

            Log.d("TASKS", "rotation target: " + target);
            Log.d("TASKS", "rotVector: " + rotVector.relativeToString() + ", controlledForwardVector: " + controlled.getForwardUnitVector().relativeToString());

            if (Math.abs(angle) < 0.08){
                Log.d("TASKS", "no need to rotate... angle:  " + angle);
                controlled.setRotationVector(Vector.ZERO_VECTOR);
                return SUCCESS;
            } else {
                Log.d("TASKS", "rotating... angle:  " + angle);
                controlled.setRotationVector(rotVector);
                return RUNNING;
            }
        }
        Log.d("TASKS", "rotateTo doesnt contains keys");
        return FAILURE;
    }

    public static BNode idleRotDamp(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                context.addPair(ROT_DAMP, 0.25f);
                return SUCCESS;
            }
        };
    }

    public static BNode attackRotDamp(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                context.addPair(ROT_DAMP, 0.75f);
                return SUCCESS;
            }
        };
    }

    public static BNode moveTowardsPoint(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, MOVE_TO)){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point target = (Point) context.getValue(MOVE_TO);

                    Vector movementVector = new Vector(controlled.getCenter(), target);
                    Log.d("TASKS", "movement vector: " + movementVector.relativeToString() + ", length: " + movementVector.getLength());

                    if (movementVector.getLength() < 100) {
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

    public static BNode aim(){
        return new BNode() {
            private int count;

            @Override
            public void construct() {
                super.construct();
                count = 0;
            }

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, LEVEL_STATE, SIGHT_BLOCKED)){

                    if ((Boolean) context.getValue(SIGHT_BLOCKED) || (Boolean) context.getValue(FRIENDLY_BLOCKING_SIGHT)) {
                        Log.d("TASKS", "sight is blocked, cannot aim " + (++count));
                        return FAILURE;
                    }

                    Log.d("TASKS", "processing aim!");
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);
                    Player player = levelState.getPlayer();

                    float projSpeed = controlled.getWeapon().getSpeed();

                    Vector vel = player.getVelocity();
                    Log.d("SHOOTING", "vel: " + vel.relativeToString());
                    Point playerVel = vel.getRelativeToTailPoint();
                    Point playerPos = player.getBoundingBox().getCenter();
                    Point aiPos = controlled.getCenter();

                    float a = square(playerVel.getX()) + square(playerVel.getY())
                            - square(projSpeed);

                    float b = 2 * (
                            (playerVel.getX() * (playerPos.getX() - aiPos.getX()))
                            + (playerVel.getY() * (playerPos.getY() - aiPos.getY()))
                    );

                    float c = square(playerPos.getX() - aiPos.getX())
                            + square(playerPos.getY() - aiPos.getY());

                    float disc = b*b - 4 * a * c;

                    float t1 = (float) (-1 * b + Math.sqrt(disc)) / (2 * a);
                    float t2 = (float) (-1 * b - Math.sqrt(disc)) / (2 * a);

                    float t = getT(t1, t2);

                    if (t<0)
                        return FAILURE;

                    Log.d("SHOOTING", "t: " + t + ", playervel: " + playerVel + ", player pos: " + playerPos);
                    Point aimPoint = playerVel.sMult(t).add(playerPos);

                    context.addPair(TARGET, aimPoint);

                    Log.d("TASKS", "t: " + t);
                    Log.d("TASKS", "aimpoint: " + aimPoint + ", playerpos: " + playerPos);
                    Log.d("TASKS", "aim vector: " + new Vector(aiPos, aimPoint).getUnitVector().getRelativeToTailPoint()
                            + ", rayVector: " + new Vector(aiPos, playerPos).getUnitVector().getRelativeToTailPoint());


                    return SUCCESS;
                } else {
                    Log.d("TASKS", "required context isn't present");
                }
                return FAILURE;
            }

            private float getT(float t1, float t2){
                if (t1 < 0)
                    return t2;
                if (t2 < 0)
                    return t1;

                return Math.min(t1,t2);
            }

            private float square(float a){
                return a*a;
            }
        };
    }

    public static BNode shoot(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsKeys(CONTROLLED_ENTITY, LEVEL_STATE, TARGET)){
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    Point aimPoint = (Point) context.getValue(TARGET);

                    Vector v = new Vector(controlled.getCenter(), aimPoint).getUnitVector();

                    Random r = new Random();

                    float angle = 0.3f * r.nextFloat();

                    v = v.rotate((float)Math.cos(angle),(float) Math.sin(angle));

                    levelState.addBullet(controlled.getWeapon().shoot(v));

                    return SUCCESS;
                }
                return FAILURE;
            }
        };
    }


    public static BNode setMoveToAsCore(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(LEVEL_STATE)){
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);

                    if (levelState.getCore() != null) {
                        context.addPair(MOVE_TO, levelState.getCore().getCenter());

                        return SUCCESS;
                    }
                }
                return FAILURE;
            }
        };
    }

    public static BNode plotPathToAnAI(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(LEVEL_STATE, CONTROLLED_ENTITY)){
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);
                    AIEntity thisEntity = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){
                        if (aiEntity.equals(thisEntity))
                            continue;

                        context.addPair(MOVE_TO, aiEntity.getCenter());
                        Status status = plotPath().process(context);

                        if (status == SUCCESS)
                            return SUCCESS;
                    }
                }
                return FAILURE;
            }
        };
    }
}
