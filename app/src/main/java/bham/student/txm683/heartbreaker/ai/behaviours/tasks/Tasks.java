package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.ILevelState;
import bham.student.txm683.heartbreaker.ai.IAIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.CollisionTools;
import bham.student.txm683.heartbreaker.utils.AStar;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.*;

public class Tasks {

    private Tasks(){

    }

    /**
     * This task will return RUNNING for the number of ticks given as an argument.
     *
     * It uses a variable in the context called "idle_time" when executing to count how many ticks are left in it's run.
     * It isn't advised to alter this value as it may cause an infinite loop
     *
     *
     * @param idlePeriod number of times that the task should return RUNNING
     * @return doNothing BNode
     */
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
                if (getStatus() == RUNNING){
                    Log.d("Tasks::doNothing", "is already running");

                    if (context.containsVariables("idle_time")){
                        int timeLeftInIdle = (int) context.getVariable("idle_time") - 1;
                        context.addVariable("idle_time", timeLeftInIdle);

                        if (timeLeftInIdle < 1){
                            Log.d("Tasks::doNothing", "has succeeded");
                            setStatus(SUCCESS);
                        } else {
                            Log.d("Tasks::doNothing", timeLeftInIdle + " time left");
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
        };
    }


    /**
     * This task will execute the arrival steering behaviour, it will slow as it approaches it's target.
     * This task will return FAILURE unless the context contains the following compulsories:
     * <ul>
     *     <li>CONTROLLED_ENTITY</li>
     * </ul>
     *
     * This task will return FAILURE unless the context contains the following variables:
     * <ul>
     *     <li>"heading"</li>
     * </ul>
     *
     * The context can have the following variables to alter the resulting behaviour:
     * <ul>
     *     <li>"arrival_distance": the distance from the target,
     *     at which the controlled entity will begin to slow it's approach. (Default: 200)</li>
     *     <li>"arrival_magnitude": the maximum length of the calculated steering vector. (Default: 100)</li>
     * </ul>
     *
     * It will add the calculated steering vector to the context with the key "arrival_steering"
     *
     *@return The BNode containing this task
     */
    public static BNode arrival(){
        return new BNode() {

            @Override
            public Status process(BContext context) {
                if (context.containsCompulsory(CONTROLLED_ENTITY) && context.containsVariables("heading")){

                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);
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

    /**
     * This task will execute the seek steering behaviour. It will head towards the heading at the maximum force
     * permanently.
     *
     * This task will return FAILURE unless the context contains the following compulsories:
     * <ul>
     *     <li>CONTROLLED_ENTITY</li>
     * </ul>
     *
     * This task will return FAILURE unless the context contains the following variables:
     * <ul>
     *     <li>"heading"</li>
     * </ul>
     *
     * The context can have the following variables to alter the resulting behaviour:
     * <ul>
     *     <li>"seek_magnitude": the maximum length of the calculated steering vector. (Default: 50)</li>
     * </ul>
     *
     * It will add the calculated steering vector to the context with the key "seek_steering"
     *
     *
     * @return The BNode containing this task
     */
    public static BNode seek(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsCompulsory(CONTROLLED_ENTITY) && context.containsVariables("heading")){
                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);
                    Point heading = (Point) context.getVariable("heading");

                    Vector vel = controlled.getVelocity();
                    Point pos = controlled.getCenter();

                    Vector desiredVel = new Vector(pos, heading).setLength(controlled.getMaxSpeed());

                    Vector steeringForce = desiredVel.vSub(vel);

                    int maxForce = (int) context.variableOrDefault("seek_magnitude", 50);

                    if (steeringForce.getLength() > maxForce)
                        steeringForce.setLength(maxForce);

                    Log.d("Tasks::seek", "force: " + steeringForce.relativeToString());
                    context.addVariable("seek_steering", steeringForce);

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    /**
     * This task will execute the courseCorrect behaviour, it will detect if an obstacle is in the way of the
     * controlled entity for a given distance, and will turn to avoid the closest obstacle if one is found.
     *
     * This task will return FAILURE unless the context contains the following compulsories:
     * <ul>
     *     <li>CONTROLLED_ENTITY</li>
     *     <li>LEVEL_STATE</li>
     * </ul>
     *
     * This task will return FAILURE unless the context contains the following variables:
     * <ul>
     *     <li>"heading"</li>
     * </ul>
     *
     * The context can have the following variables to alter the resulting behaviour:
     * <ul>
     *     <li>"evasion_magnitude": the maximum length of the calculated steering vector. (Default: 50)</li>
     * </ul>
     *
     * It will add the calculated steering vector to the context with the key "evasion_steering"
     *
     *
     *@return The BNode containing this task
     */
    public static BNode courseCorrect(){
        return new BNode() {

            @Override
            public Status process(BContext context) {

                if (context.containsCompulsory(CONTROLLED_ENTITY, LEVEL_STATE) && context.containsVariables("heading")){

                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);

                    Point heading = (Point) context.getVariable("heading");

                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);

                    Vector steeringAxis = CollisionTools.getPathAroundObstacle(controlled, heading, levelState.getAvoidables());

                    if (!steeringAxis.equals(Vector.ZERO_VECTOR)){
                        //correction needs to take place

                        int maxForce = (int) context.variableOrDefault("evasion_magnitude", 50);

                        Log.d("Tasks::courseCorrect", "steeringForce: " + steeringAxis.setLength(maxForce).relativeToString());

                        context.addVariable("evasion_steering", steeringAxis.setLength(maxForce));
                    } else {
                        Log.d("Tasks::courseCorrect", "no obstacles in way");

                        if (levelState.mapToMesh(controlled.getCenter().add(controlled.getVelocity()
                                .sMult(0.1f).getRelativeToTailPoint())) == -1){
                            controlled.setVelocity(Vector.ZERO_VECTOR);
                        }
                    }

                    setStatus(SUCCESS);
                    return SUCCESS;
                }
                Log.d("Tasks::courseCorrect", "FAILED");

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    /**
     * This task will use the AStar class to plot a path to the provided destination using the navmesh
     *
     * This task will return FAILURE unless the context contains the following compulsories:
     * <ul>
     *     <li>CONTROLLED_ENTITY</li>
     *     <li>CURRENT_MESH</li>
     *     <li>LEVEL_STATE</li>
     *     <li>MOVE_TO</li>
     * </ul>
     *
     * This task will add a boolean for if plotting failed to the context under the key "plottingFailed",
     * and a compulsory to the PATH key.
     *
     * @param returnIncompletePath Even if a complete path to the target cannot be established,
     *                             if this argument is true, a path getting as close as possible
     *                             to the target will be returned
     * @return The BNode containing this task
     */
    public static BNode plotPath(boolean returnIncompletePath){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsCompulsory(CONTROLLED_ENTITY, CURRENT_MESH, LEVEL_STATE, MOVE_TO)){
                    Log.d("Tasks::plotPath", "plotting...");

                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);

                    AStar a = new AStar(context,
                            levelState.getRootMeshPolygons(),
                            levelState.getMeshGraph());

                    boolean plotted = a.plotPath(returnIncompletePath);

                    Log.d("Tasks::plotPath", "plotting path: " + plotted);

                    if (plotted) {
                        context.addVariable("plottingFailed", false);
                        setStatus(SUCCESS);
                        return SUCCESS;
                    } else {
                        context.addVariable("plottingFailed", true);
                    }
                }

                Log.d("PROCESS", "plotting path has failed");
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    /**
     * This task will find a mesh that is adjacent to the mesh that the player is currently in
     *
     * This task will return FAILURE unless the context contains the following compulsories:
     * <ul>
     *     <li>LEVEL_STATE</li>
     * </ul>
     *
     * @return The BNode containing this task
     */
    public static BNode findMeshAdjacentToPlayer(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsCompulsory(LEVEL_STATE)){

                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);

                    Graph<Integer> graph = levelState.getMeshGraph();

                    int playerMesh = levelState.getPlayer().getMesh();

                    if (playerMesh < 0){
                        setStatus(FAILURE);
                        return FAILURE;
                    }

                    List<Node<Integer>> neighbours = graph.getNode(playerMesh).getNeighbours();

                    if (neighbours.size() == 0){
                        setStatus(FAILURE);
                        return FAILURE;
                    }

                    Random r = new Random();

                    MeshPolygon meshPolygon = levelState.getRootMeshPolygons().get(neighbours.get(r.nextInt(neighbours.size())).getNodeID());

                    if (meshPolygon == null){
                        setStatus(FAILURE);
                        return FAILURE;
                    }

                    Point p = meshPolygon.getCenter();

                    context.addCompulsory(MOVE_TO, p);

                    Log.d("Tasks::adjacentMeshToPlayer", ((IAIEntity)context.getCompulsory(CONTROLLED_ENTITY)).getName() + " is finding an adjacent mesh");

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    /**
     *
     * This task will perform a patrol route out of the provided patrol path.
     *
     * @param patrolPathPoints A list containing the patrol points in desired order. It will form a loop
     *                         from the last point to the first.
     * @return The BNode for this task
     */
    public static BNode patrol(List<Integer> patrolPathPoints){
        return new BNode() {

            List<Integer> patrolPath;

            @Override
            public void construct() {
                super.construct();
                patrolPath = patrolPathPoints;
            }

            @Override
            public Status process(BContext context) {
                if (context.containsCompulsory(LEVEL_STATE, CONTROLLED_ENTITY)){
                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);
                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);

                    if (!context.containsVariables("patrol")){
                        context.addVariable("patrol", 0);
                    }

                    int patrolIdx = (int) context.getVariable("patrol");

                    if (context.containsVariables("plottingFailed") && ((boolean) context.getVariable("plottingFailed"))){
                        patrolIdx++;
                        if (patrolIdx >= patrolPath.size())
                            patrolIdx = 0;
                    }

                    MeshPolygon meshPolygon = levelState.getRootMeshPolygons().get(patrolPath.get(patrolIdx));

                    if (meshPolygon == null){
                        setStatus(FAILURE);
                        return FAILURE;
                    }

                    Point point = meshPolygon.getCenter();

                    float distance = new Vector(controlled.getCenter(), point).getLength();

                    if (controlled.getBoundingBox().intersecting(point)){
                        Log.d("Tasks::patrol", "arrived at: " + patrolPath.get(patrolIdx));
                        patrolIdx++;

                        if (patrolIdx >= patrolPath.size()){
                            patrolIdx = 0;
                        }
                    } else {
                        Log.d("Tasks::patrol", "not arrived to patrol point in mesh " + patrolPath.get(patrolIdx) + " yet, " +
                                "distance to go: " + distance);
                    }

                    context.addVariable("patrol", patrolIdx);

                    Log.d("Tasks::patrol", "heading to: " + patrolPath.get(patrolIdx));

                    context.addCompulsory(MOVE_TO, point);

                    return Status.SUCCESS;
                }
                return Status.FAILURE;
            }
        };
    }

    /**
     * This task will pick a random mesh out of the mesh graph and set it as the MOVE_TO compulsory
     * @return The BNode for this task
     */
    public static BNode pickRandomMesh(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsCompulsory(LEVEL_STATE)){
                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);

                    Random r = new Random();

                    List<Integer> meshIdList = new ArrayList<>(levelState.getRootMeshPolygons().keySet());

                    int id = meshIdList.get(r.nextInt(meshIdList.size()));

                    MeshPolygon meshPolygon = levelState.getRootMeshPolygons().get(id);

                    if (meshPolygon != null){
                        context.addCompulsory(MOVE_TO, meshPolygon.getCenter());

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
                if (context.containsCompulsory(CONTROLLED_ENTITY, PATH)){

                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);
                    List<Point> path = ((PathWrapper) context.getCompulsory(PATH)).getIPath();

                    //project ai into the future

                    float velocityTimeStep = (float) context.variableOrDefault("path_velocity_time_step", 0.2f);

                    Vector vel = controlled.getVelocity().sMult(velocityTimeStep);

                    if (vel.getLength() < 10){
                        vel = controlled.getForwardUnitVector().setLength(10);
                    }

                    Point futurePos = controlled.getCenter().add(vel.getRelativeToTailPoint());

                    context.addVariable("future_position", futurePos);

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

                        int closesIdx = CollisionTools.getClosestPointOnPathIdx(futurePos, path);

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
                if (context.containsCompulsory(MOVE_TO, CONTROLLED_ENTITY)){

                    Log.d("TASKS", "rotateTo contains keys");
                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);


                    MoveableEntity player = ((ILevelState) context.getCompulsory(LEVEL_STATE)).getPlayer();
                    float wiggleRoom = CollisionTools.getWiggleRoom(player, controlled);

                    Vector rotVector = new Vector(controlled.getCenter(), player.getCenter());

                    float angle = Vector.calculateAngleBetweenVectors(controlled.getShootingVector().getUnitVector(),
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
                if (context.containsCompulsory(CONTROLLED_ENTITY, LEVEL_STATE, SIGHT_BLOCKED)){

                    if ((Boolean) context.getCompulsory(SIGHT_BLOCKED) || (Boolean) context.getCompulsory(FRIENDLY_BLOCKING_SIGHT)) {
                        Log.d("TASKS", "sight is blocked, cannot aim " + (++count));
                        return FAILURE;
                    }

                    Log.d("TASKS", "processing aim!");
                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);
                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);
                    MoveableEntity player = levelState.getPlayer();

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

                    context.addCompulsory(TARGET, v.getHead());

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

                if (context.containsCompulsory(CONTROLLED_ENTITY, LEVEL_STATE, TARGET)){
                    ILevelState levelState = (ILevelState) context.getCompulsory(LEVEL_STATE);
                    IAIEntity controlled = (IAIEntity) context.getCompulsory(CONTROLLED_ENTITY);

                    Log.d("SHOOT", "SHooting");

                    levelState.addBullet(controlled.getWeapon().shoot(controlled.getShootingVector()));

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
                if (context.containsCompulsory(LEVEL_STATE)) {

                    context.addVariable("heading", ((ILevelState)context.getCompulsory(LEVEL_STATE)).getPlayer().getCenter());

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }
}