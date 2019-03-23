package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.utils.AStar;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.*;

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
                context.removeVariables("idle_time");
            }

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY)){
                    if (getStatus() == RUNNING){
                        Log.d("TASKS doNothing", "is already running");

                        if (context.containsVariables("idle_time")){
                            int timeLeftInIdle = (int) context.getVariable("idle_time") - 1;
                            context.addVariable("idle_time", timeLeftInIdle);

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
                        if (context.containsVariables("idle_time")) {
                            Log.d("TASKS doNothing", "setting movement to zero");
                            int timePerIdle = (int) context.getVariable("idle_time");
                            context.addVariable("idle_time", timePerIdle);

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
            }
        };
    }

    public static BNode applyMovementForces(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsKeys(CONTROLLED_ENTITY)){
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    Vector force = Vector.ZERO_VECTOR;

                    PriorityQueue<Pair<Integer, Vector>> forces = new PriorityQueue<>(10, (a, b) -> {
                        if (a.first < b.first)
                            return -1;
                        else if (a.second.equals(b.second))
                            return 0;
                        return 1; });

                    if (context.containsVariables("evasion_steering")){
                        forces.add(new Pair<>(1, (Vector) context.getVariable("evasion_steering")));
                        force = force.vAdd((Vector) context.getVariable("evasion_steering"));
                    }

                    if (context.containsVariables("path_steering")){
                        forces.add(new Pair<>(2, (Vector) context.getVariable("path_steering")));

                        if (force.equals(Vector.ZERO_VECTOR)){
                            force = force.vAdd((Vector) context.getVariable("path_steering"));
                        }
                    }

                    if (context.containsVariables("arrival_steering")){
                        forces.add(new Pair<>(3, (Vector) context.getVariable("arrival_steering")));
                    } else if (context.containsVariables("seek_steering")){
                        forces.add(new Pair<>(3, (Vector) context.getVariable("seek_steering")));
                    }


                    int numForces = forces.size();

                    if (numForces > 1) {
                        /*while (!forces.isEmpty()) {

                            Pair<Integer, Vector> pair = forces.poll();
                            int priority = pair.first;
                            Vector currentForce = pair.second;

                            if (priority == 1){
                                force = force.vAdd(currentForce.sMult(0.75f));
                            }
                        }*/
                    } else if (numForces == 1){
                        force = force.vAdd(forces.poll().second);
                    }

                    controlled.addForce(force);

                    context.removeVariables("evasion_steering", "path_steering", "arrival_steering", "seek_steering");

                    setStatus(SUCCESS);
                    return SUCCESS;
                }
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode arrival(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY) && context.containsVariables("heading")){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point heading = (Point) context.getVariable("heading");

                    Vector desiredVel = new Vector(controlled.getCenter(), heading);
                    float distance = desiredVel.getLength();

                    final float SLOWING_DISTANCE = 200;

                    float rampedSpeed = controlled.getMaxSpeed() * distance/SLOWING_DISTANCE;
                    float clampedSpeed = Math.min(rampedSpeed, controlled.getMaxSpeed());

                    desiredVel = desiredVel.setLength(clampedSpeed);

                    Vector steering = desiredVel.vSub(controlled.getVelocity());

                    if (steering.getLength() > 100){
                        steering.setLength(100);
                    }

                    context.addVariable("arrival_steering", steering);

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode seek(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY) && context.containsVariables("heading")){
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point heading = (Point) context.getVariable("heading");

                    Vector vel = controlled.getVelocity();
                    Point pos = controlled.getCenter();

                    Vector desiredVel = new Vector(pos, heading).setLength(controlled.getMaxSpeed());

                    Vector steeringForce = desiredVel.vSub(vel);

                    int maxForce = 50;

                    if (steeringForce.getLength() > maxForce)
                        steeringForce.setLength(maxForce);

                    context.addVariable("seek_steering", steeringForce);

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode courseCorrect(){
        return new BNode() {

            @Override
            public Status process(BContext context) {


                if (context.containsKeys(CONTROLLED_ENTITY, LEVEL_STATE) && context.containsVariables("heading")){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    Point heading = (Point) context.getVariable("heading");

                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);

                    CollisionManager collisionManager = ((LevelState) context.getValue(LEVEL_STATE)).getCollisionManager();

                    Vector steeringAxis = collisionManager.getPathAroundObstacle(controlled, heading);

                    //Vector steeringAxis = collisionManager.movingTargetAvoidanceForTanks(controlled);

                    if (!steeringAxis.equals(Vector.ZERO_VECTOR)){
                        //correction needs to take place

                        Log.d("BEH", "courseCorrect: steeringForce: " + steeringAxis.setLength(100).relativeToString());

                        context.addVariable("evasion_steering", steeringAxis.sMult(50));
                    } else {
                        Log.d("AVOID", "no obstacles in way");

                        if (levelState.mapToMesh(controlled.getCenter().add(controlled.getVelocity().sMult(0.1f).getRelativeToTailPoint())) == -1){
                            controlled.setVelocity(Vector.ZERO_VECTOR);
                        }
                    }

                    setStatus(SUCCESS);
                    return SUCCESS;
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
                    //Log.d("TASK plotPath", "levelstate is null: " + (context.getValue(LEVEL_STATE) == null));

                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);
                    //context.addPair(MOVE_TO, levelState.getPlayer().getCenter());

                    AStar a = new AStar((AIEntity) context.getValue(CONTROLLED_ENTITY),
                            levelState.getRootMeshPolygons(),
                            levelState.getMeshGraph());

                    boolean plotted = a.plotPath();

                    Log.d("TASK", "plotted: " + plotted);

                    if (plotted) {
                        context.addVariable("plottingFailed", false);
                        setStatus(SUCCESS);
                        return SUCCESS;
                    } else {
                        context.addVariable("plottingFailed", true);
                    }
                }
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode patrol(){
        return new BNode() {

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
    }

    public static BNode plotToRandomMesh(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsKeys(LEVEL_STATE)){
                    LevelState levelState = (LevelState) context.getValue(LEVEL_STATE);

                    Random r = new Random();

                    List<Integer> meshIdList = new ArrayList<>(levelState.getRootMeshPolygons().keySet());

                    int id = meshIdList.get(r.nextInt(meshIdList.size()));

                    MeshPolygon meshPolygon = levelState.getRootMeshPolygons().get(id);

                    if (meshPolygon != null){
                        context.addPair(MOVE_TO, meshPolygon.getRandomPointInMesh());

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

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    List<Point> path = ((PathWrapper) context.getValue(PATH)).getIPath();

                    //project ai 0.5 seconds into the future

                    Vector vel = controlled.getVelocity().sMult(0.1f);

                    if (vel.getLength() < 50){
                        vel = controlled.getForwardUnitVector().setLength(50);
                    }

                    Point futurePos = controlled.getCenter().add(vel.getRelativeToTailPoint());

                    Point closestPoint;

                    if (path.size() == 0){
                        Log.d("Follow", "basePath size is zero");
                        setStatus(SUCCESS);
                        return SUCCESS;
                    }  else {
                        Log.d("Follow", "basePath size is: " + path.size());

                        int closesIdx = CollisionManager.getClosestPointOnPathIdx(futurePos, path);

                        if (closesIdx >= 0){

                            closestPoint = path.get(closesIdx);

                            while(closesIdx < path.size()-1 && AStar.calculateEuclideanHeuristic(futurePos, closestPoint) < 75){
                                closesIdx++;

                                closestPoint = path.get(closesIdx);
                            }

                            if (AStar.calculateEuclideanHeuristic(futurePos, closestPoint) < 75 && closesIdx == path.size()-1){
                                //arrived at destination
                                closestPoint = path.get(path.size()-1);
                            }

                        } else {
                            Log.d("Follow", "closestidx < 0");
                            setStatus(FAILURE);
                            return FAILURE;
                        }
                    }

                    Log.d("CURVE", "futurepos: " + futurePos + ", closestPos: " + closestPoint);

                    int maxForce = 50;

                    context.addVariable("heading", closestPoint);

                    context.addVariable("closest_point", closestPoint);

                    //Log.d("TANKK", "distance to end: " + new Vector(controlled.getCenter(), basePath.get(basePath.size()-1)).getLength());

                    Vector steeringVector = new Vector(futurePos, closestPoint);

                    //steeringVector = Vector.proj(steeringVector, controlled.getForwardUnitVector());

                    //Vector movementForce = controlled.getForwardUnitVector().setLength(maxForce);

                    //float t = steeringVector.getLength() / maxForce;

                    //movementForce = movementForce.sMult(1-t);


                    if (steeringVector.getLength() > maxForce){
                        steeringVector.setLength(maxForce);
                    } else if (steeringVector.getLength() < 5)
                        steeringVector.setLength(5);

                    float distanceToEndNode = new Vector(controlled.getCenter(), path.get(path.size()-1)).getLength();
                    if (distanceToEndNode < 75){
                        context.addVariable("arrived", true);
                        context.addVariable("arriving", false);

                        Log.d("Follow", "arrived");

                    } else if (distanceToEndNode < 200){
                        context.addVariable("arriving", true);
                        context.addVariable("arrived", false);
                        Log.d("Follow", "arriving");
                    } else {
                        context.addVariable("arrived", false);
                        context.addVariable("arriving", false);
                        Log.d("Follow", "still travelling");
                    }

                    context.addVariable("path_steering", steeringVector);

                    Log.d("BEH", "follow: steeringForce: " + steeringVector.relativeToString());

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


            Player player = ((LevelState) context.getValue(LEVEL_STATE)).getPlayer();
            float wiggleRoom = CollisionManager.getWiggleRoom(player, controlled);

            Vector rotVector = new Vector(controlled.getCenter(), player.getCenter());

            float angle = Vector.calculateAngleBetweenVectors(((TankBody)controlled.getShape()).getTurretFUnit(),
                    rotVector);

            Log.d("TASKS", "rotation target: " + target);
            Log.d("TASKS", "rotVector: " + rotVector.relativeToString() + ", controlledForwardVector: " + controlled.getForwardUnitVector().relativeToString());

            Log.d("TANKK", "wiggleRoom: " + wiggleRoom + ", angle: " + angle);
            if (Math.abs(angle) < wiggleRoom){
                Log.d("TASKS", "no need to applyRotationalForces... angle:  " + angle);
                controlled.setRotationVector(Vector.ZERO_VECTOR);
                return SUCCESS;
            } else {
                Log.d("TASKS", "rotating... angle:  " + angle);
                controlled.setRotationVector(rotVector.getUnitVector());
                return RUNNING;
            }
        }
        Log.d("TASKS", "rotateTo doesnt contains keys");
        return FAILURE;
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

                    Random r = new Random();

                    float angle = 0.3f * r.nextFloat();

                    Vector v = new Vector(controlled.getCenter(), aimPoint);
                    v = v.rotate((float)Math.cos(angle),(float) Math.sin(angle));

                    context.addPair(TARGET, v.getHead());

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

                    levelState.addBullet(controlled.getWeapon().shoot(((TankBody) controlled.getShape()).getShootingVector()));

                    return SUCCESS;
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
