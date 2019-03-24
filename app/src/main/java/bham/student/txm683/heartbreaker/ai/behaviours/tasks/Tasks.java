package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Overlord;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.SpatialBin;
import bham.student.txm683.heartbreaker.utils.AStar;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.*;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.*;

public class Tasks {

    private Tasks(){

    }

    public static BNode doNothing(int idlePeriod){
        return new BNode() {
            int cooldown;

            @Override
            public void construct() {
                super.construct();

                this.cooldown = idlePeriod;
            }

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
                        context.addVariable("idle_time", cooldown);
                        setStatus(RUNNING);
                    }

                    return getStatus();
                }
                Log.d("TASKS", "doNothing has failed");
                setStatus(FAILURE);
                return FAILURE;
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

                    boolean evading = false;
                    if (context.containsVariables("evasion_steering")){
                        forces.add(new Pair<>(1, (Vector) context.getVariable("evasion_steering")));
                        force = force.vAdd((Vector) context.getVariable("evasion_steering"));
                        evading = true;
                    }

                    if (context.containsVariables("path_steering")) {
                        forces.add(new Pair<>(2, (Vector) context.getVariable("path_steering")));

                        if (force.equals(Vector.ZERO_VECTOR)) {

                            Vector v = (Vector) context.getVariable("path_steering");

                            if (evading)
                                v = v.sMult(0.5f);

                            force = force.vAdd(v);
                        }
                    }

                    if (context.containsVariables("arrival_steering")) {
                        forces.add(new Pair<>(3, (Vector) context.getVariable("arrival_steering")));
                    } else if (context.containsVariables("seek_steering")) {
                        forces.add(new Pair<>(3, (Vector) context.getVariable("seek_steering")));
                    }



                    int numForces = forces.size();

                    if (numForces > 1) {
                        while (!forces.isEmpty()) {

                            Pair<Integer, Vector> pair = forces.poll();
                            Vector currentForce = pair.second;

                            force = force.vAdd(currentForce.sMult(0.75f));
                        }
                    } else if (numForces == 1){
                        force = force.vAdd(forces.poll().second);
                    }

                    controlled.addForce(force);

                    Log.d("HEALER", "applying force to " + controlled.getName());

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

                    float SLOWING_DISTANCE = (int) context.variableOrDefault("arrival_distance", 200);

                    float rampedSpeed = controlled.getMaxSpeed() * distance/SLOWING_DISTANCE;
                    float clampedSpeed = Math.min(rampedSpeed, controlled.getMaxSpeed());

                    desiredVel = desiredVel.setLength(clampedSpeed);

                    Vector steering = desiredVel.vSub(controlled.getVelocity());

                    int maxForce = (int) context.variableOrDefault("arrival_magnitude", 100);

                    if (steering.getLength() > maxForce){
                        steering.setLength(maxForce);
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

                    int maxForce = (int) context.variableOrDefault("seek_magnitude", 50);

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

                        int maxForce = (int) context.variableOrDefault("evasion_magnitude", 50);

                        Log.d("BEH", "courseCorrect: steeringForce: " + steeringAxis.setLength(maxForce).relativeToString());

                        context.addVariable("evasion_steering", steeringAxis.setLength(maxForce));
                    } else {
                        Log.d("AVOID", "no obstacles in way");

                        if (levelState.mapToMesh(controlled.getCenter().add(controlled.getVelocity()
                                .sMult(0.1f).getRelativeToTailPoint())) == -1){
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

    public static BNode plotPathToMeshAdjacentToPlayer(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                //TODO implement

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

                    Point point = levelState.getRootMeshPolygons().get(patrolPath.get(patrolIdx)).getCenter();

                    float distance = new Vector(controlled.getCenter(), point).getLength();

                    if (controlled.getBoundingBox().intersecting(point)){
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

    public static BNode pickRandomMesh(){
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
                        context.addPair(MOVE_TO, meshPolygon.getCenter());

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

            private void arrived(BContext context){
                context.addVariable("arrived", true);
                context.addVariable("arriving", false);

                Log.d("Follow", "arrived");
            }

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, PATH)){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    List<Point> path = ((PathWrapper) context.getValue(PATH)).getIPath();

                    //project ai into the future

                    float velocityTimeStep = (float) context.variableOrDefault("path_velocity_time_step", 0.04f);

                    Vector vel = controlled.getVelocity().sMult(velocityTimeStep);

                    if (vel.getLength() < 10){
                        vel = controlled.getForwardUnitVector().setLength(10);
                    }

                    Point futurePos = controlled.getCenter().add(vel.getRelativeToTailPoint());

                    Point closestPoint;

                    int distanceForArrived = (int) context.variableOrDefault("path_distance_for_arrived", 75);

                    if (path.size() == 0){
                        Log.d("Follow", "basePath size is zero");
                        arrived(context);
                        setStatus(SUCCESS);
                        return SUCCESS;
                    } else if (path.size() == 2) {

                        float distance1 = new Vector(futurePos, path.get(1)).getLength();

                        if (distance1 < distanceForArrived){
                            arrived(context);
                            setStatus(SUCCESS);
                            return SUCCESS;
                        } else {
                            closestPoint = path.get(1);
                        }
                    } else {
                        Log.d("Follow", "basePath size is: " + path.size());

                        int closesIdx = CollisionManager.getClosestPointOnPathIdx(futurePos, path);

                        if (closesIdx >= 0){

                            closestPoint = path.get(closesIdx);

                            while(closesIdx < path.size()-1 && AStar.calculateEuclideanHeuristic(futurePos, closestPoint) < 75){
                                closesIdx++;

                                closestPoint = path.get(closesIdx);
                            }

                            if (AStar.calculateEuclideanHeuristic(futurePos, closestPoint) < distanceForArrived && closesIdx == path.size()-1){
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

                    int maxForce = 30;

                    context.addVariable("heading", closestPoint);

                    context.addVariable("closest_point", closestPoint);

                    Vector steeringVector = new Vector(futurePos, closestPoint);


                    if (steeringVector.getLength() > maxForce){
                        steeringVector.setLength(maxForce);
                    } else if (steeringVector.getLength() < 5)
                        steeringVector.setLength(5);

                    float distanceToEndNode = new Vector(controlled.getCenter(), path.get(path.size()-1)).getLength();

                    int distanceForArrival = (int) context.variableOrDefault("path_distance_for_arrival", 200);

                    if (controlled.getBoundingBox().intersecting(path.get(path.size()-1))){
                        arrived(context);

                    } else if (distanceToEndNode < distanceForArrival){
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

    public static BNode rotateToTarget(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(MOVE_TO, CONTROLLED_ENTITY)){

                    Log.d("TASKS", "rotateTo contains keys");
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);


                    Player player = ((LevelState) context.getValue(LEVEL_STATE)).getPlayer();
                    float wiggleRoom = CollisionManager.getWiggleRoom(player, controlled);

                    Vector rotVector = new Vector(controlled.getCenter(), player.getCenter());

                    float angle = Vector.calculateAngleBetweenVectors(((TankBody)controlled.getShape()).getTurretFUnit(),
                            rotVector);

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

                    Random r = new Random();

                    float maxInaccuracy = (float) context.variableOrDefault("aim_max_inaccuracy_angle", 0.03f);

                    float angle = maxInaccuracy * r.nextFloat();

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

                    Log.d("SHOOT", "SHooting");

                    levelState.addBullet(controlled.getWeapon().shoot(((TankBody) controlled.getShape()).getShootingVector()));

                    return SUCCESS;
                }
                return FAILURE;
            }
        };
    }

    public static BNode setHeadingAsPlayer(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(LEVEL_STATE)) {

                    context.addVariable("heading", ((LevelState)context.getValue(LEVEL_STATE)).getPlayer());

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode detonate(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY)){

                    AIEntity controlled = ((AIEntity) context.getValue(CONTROLLED_ENTITY));

                    controlled.setHealth(0);

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode flashRed(int duration){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY)){

                    AIEntity controlled = ((AIEntity)context.getValue(CONTROLLED_ENTITY));

                    context.addVariable("flash_duration", duration);

                    if (getStatus() != RUNNING){
                        context.addVariable("flash_remaining", duration);
                    }

                    int flashRemaining = (int) context.getVariable("flash_remaining");

                    if (flashRemaining > 0){
                        flashRemaining--;

                        if (flashRemaining % 2 == 0){
                            controlled.setColor(Color.RED);
                        } else {
                            controlled.setColor(Color.BLACK);
                        }

                    } else {
                        setStatus(SUCCESS);
                        return SUCCESS;
                    }

                    context.addVariable("flash_remaining", flashRemaining);

                    setStatus(RUNNING);
                    return RUNNING;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode findAIToHeal(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(OVERLORD, CONTROLLED_ENTITY)){
                    Overlord overlord = (Overlord) context.getValue(OVERLORD);
                    AIEntity thisEntity = (AIEntity) context.getValue(CONTROLLED_ENTITY);

                    float minRatio = (float) context.variableOrDefault("healing_min_ratio", 0.04f);
                    for (AIEntity aiEntity : overlord.getAliveEntities()){
                        if (aiEntity.equals(thisEntity))
                            continue;

                        if (aiEntity.getRadioHealthLeft() < minRatio) {

                            Log.d("HEALER", thisEntity.getName() + " plotting to " + aiEntity.getName());
                            context.addPair(MOVE_TO, aiEntity.getCenter());

                            setStatus(SUCCESS);
                            return SUCCESS;
                        }
                    }
                } else {
                    Log.d("HEALER", "plot path to ai doesnt have required fields");
                }

                Log.d("HEALER", "plot path to ai might not have any entities with low health");
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode healField(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsKeys(CONTROLLED_ENTITY, LEVEL_STATE)){
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    CollisionManager collisionManager = ((LevelState) context.getValue(LEVEL_STATE)).getCollisionManager();

                    Log.d("HEALER", "healfield executing");
                    for (SpatialBin spatialBin : collisionManager.getSpatialBins()){
                        if (spatialBin.contains(controlled)){

                            for (Collidable collidable : spatialBin.getTemps()){

                                if (collidable instanceof AIEntity){
                                    float ratioLeft = ((AIEntity) collidable).getRadioHealthLeft();

                                    float minRatio = (float) context.variableOrDefault("healing_min_ratio", 0.04f);

                                    if (Float.compare(ratioLeft, minRatio) < 1){

                                        ((AIEntity) collidable).restoreHealth(60);
                                        Log.d("HEALER", "healfield restoring health of " + collidable.getName());

                                        setStatus(SUCCESS);
                                        return SUCCESS;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.d("HEALER", "healfield failed as required variables are missing");
                }
                Log.d("HEALER", "healfield failed");
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }
}
