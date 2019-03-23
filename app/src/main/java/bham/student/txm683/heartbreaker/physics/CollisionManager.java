package bham.student.txm683.heartbreaker.physics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.entities.*;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.physics.fields.DoorField;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Key;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.*;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;
import static bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier.RECTANGLE;

public class CollisionManager {

    private List<SpatialBin> spatialBins;

    private static final float PUSH_VECTOR_ERROR = 0.001f;

    private LevelState levelState;

    private HashSet<String> checkedPairNames;
    private HashSet<String> doorsToOpen;

    private static String TAG = "hb::CollisionManager";

    private BenchMarker benchMarker;

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;

        this.spatialBins = new ArrayList<>();

        initSpatPatV4();

        benchMarker = new BenchMarker();
    }

    public void checkCollisions(){
        benchMarker.begin();
        applySpatPatV2();
        benchMarker.output("collision rough-grain");

        benchMarker.begin();
        fineGrainCollisionDetection();
        benchMarker.output("collision fine-grain");

        benchMarker.begin();
        aiSight();
        benchMarker.output("ai sight");

    }

    public void drawBins(Canvas canvas, Point renderOffset){
        Paint paint = new Paint();

        for (SpatialBin spatialBin : spatialBins){
            paint.setColor(ColorScheme.randomColor());

            spatialBin.getBoundingBox().draw(canvas, renderOffset, paint);
        }
    }

    private void fillBins(){
        //add each static to the permanent list in the correct spatial bin
        for (Collidable collidable : levelState.getStaticCollidables()){

            for (SpatialBin bin : spatialBins){
                if (bin.getBoundingBox().intersecting(collidable.getBoundingBox())){

                    if (collidable instanceof Door){
                        bin.addPermanent(((Door) collidable).getPrimaryField());
                    }

                    //if the collidable intersects this bin's bounding box, add it to the bin's permanent list
                    bin.addPermanent(collidable);
                }
            }
        }
    }

    private void initSpatPatV4(){

        UniqueID uniqueID = new UniqueID();

        int numHCells = 4;
        int numVCells = 4;

        int cellWidth = (int) levelState.getMap().getWidth()/numHCells;
        int cellHeight = (int) levelState.getMap().getHeight()/numVCells;

        for (int i = 1; i <= numHCells; i++){
            int l = (i-1) * cellWidth;
            int r = i * cellWidth;

            for (int j = 1; j <= numVCells; j++){
                int t = (j-1) * cellHeight;
                int b = j * cellHeight;

                spatialBins.add(new SpatialBin(uniqueID.id(), new BoundingBox(l,t,r,b)));
            }
        }

        fillBins();
    }

    private void applySpatPatV2(){
        //clear last tick's collision bins
        for (SpatialBin bin : spatialBins){
            bin.clearTemps();
        }

        for (Collidable collidable : levelState.getNonStaticCollidables()){

            if (!addToBin(collidable)){
                Log.d("hb::CollisionManager", collidable.getName() + " is not in a room");

                if (collidable instanceof MoveableEntity){
                    moveEntityCenter(collidable, ((MoveableEntity) collidable).getSpawn());
                    addToBin(collidable);
                }
            }
        }
    }

    public List<SpatialBin> getSpatialBins() {
        return spatialBins;
    }

    private boolean addToBin(Collidable collidable){
        boolean added = false;
        for (SpatialBin bin : spatialBins){
            if (bin.getBoundingBox().intersecting(collidable.getBoundingBox())){
                //if the collidable intersects this bin's bounding box, add it to the bin's temp list
                bin.addTemp(collidable);
                added = true;
            }
        }
        return added;
    }

    private void fineGrainCollisionDetection(){
        checkedPairNames = new HashSet<>();

        doorsToOpen = new HashSet<>();

        Collidable firstCollidable;
        Collidable secondCollidable;

        Vector pushVector;

        //iterate through bins
        List<Collidable> bin;
        for (SpatialBin spatialBin : spatialBins){
            bin = spatialBin.getCollidables();

            if (bin.size() > 1){

                //check each pair of entities in current bin
                for (int i = 0; i < bin.size() - 1; i++){
                    for (int j = i+1; j < bin.size(); j++) {

                        firstCollidable = bin.get(i);
                        secondCollidable = bin.get(j);

                        //if both entities are static or not solid, skip
                        if (firstCollidable instanceof Core || secondCollidable instanceof Core){
                            //do nothing
                        } else if (firstCollidable instanceof Damageable || secondCollidable instanceof Damageable){
                            //do nothing
                        } else if ((!firstCollidable.canMove() && !secondCollidable.canMove()) ||
                                (!firstCollidable.isSolid() && !secondCollidable.isSolid())) {
                            continue;
                        }

                        //if the two entities have already been checked together for collisions, skip
                        if (checkedPairNames.contains(firstCollidable.getName() + secondCollidable.getName())) {
                            continue;
                        }

                        //start of collision checking
                        if (!firstCollidable.isSolid() || !secondCollidable.isSolid()) {
                            //one of the entities is not a solid and the collision does not need to be resolved

                            Collidable solidEntity = firstCollidable.isSolid() ? firstCollidable : secondCollidable;
                            Collidable nonSolidEntity = firstCollidable.isSolid() ? secondCollidable : firstCollidable;

                            if (nonSolidEntity instanceof Explosion){
                                pushVector = collisionCheckCircleAndPolygon(((Explosion) nonSolidEntity).getCircle(), solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveExplosion((Explosion) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof Projectile){
                                pushVector = collisionCheckCircleAndPolygon(((Projectile) nonSolidEntity).getCircle(), solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveProjectileHit((Projectile) nonSolidEntity, solidEntity, pushVector);
                                }
                            } else if (nonSolidEntity instanceof DoorField){
                                pushVector = collisionCheckTwoPolygonalCollidables(nonSolidEntity, solidEntity);
                                //Log.d("hb::SolidNonSolid", "solid: " + solidEntity.getName() + ", and nonSolid: " + nonSolidEntity.getName() + " with owner " + ((DoorField) nonSolidEntity).getOwner());
                                //Log.d("hb:: DoorCollision", pushVector.relativeToString());

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveDoorFieldActivation((DoorField) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof Pickup){
                                pushVector = collisionCheckTwoPolygonalCollidables(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolvePickupActivation((Pickup) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof Portal){
                                pushVector = collisionCheckCircleAndPolygon(((Portal) nonSolidEntity).getCircle(), solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    resolvePortalCollision((Portal) nonSolidEntity, solidEntity);
                                }
                            }

                        } else {
                            //both collidables are solid
                            pushVector = collisionCheckTwoPolygonalCollidables(firstCollidable, secondCollidable);

                            if (!pushVector.equals(Vector.ZERO_VECTOR)) {
                                //collision occurred
                                resolveSolidsCollision(firstCollidable, secondCollidable, pushVector, bin);
                            }
                        }
                        //add entity names to the checked names set so that they aren't checked twice
                        addCheckedPairNames(firstCollidable, secondCollidable);
                    }
                }
            }
        }

        for (Door door : levelState.getMap().getDoors().values()){
            if (doorsToOpen.contains(door.getName())) {
                door.setOpen(true);
            }else
                door.setOpen(false);
        }
    }

    private void resolvePortalCollision(Portal portal, Collidable collidable){
        if (collidable instanceof Player){

            if (portal.isActive()){
                Log.d("PORTAL COLLISION", "portal active and player in bounds");
                portal.setPlayerInBounds(true);
            } else {
                Log.d("PORTAL COLLISION", "player in bounds");
            }
        }
    }

    private void resolvePickupActivation(Pickup pickup, Collidable collidable){
        if (collidable instanceof Player){
            switch (pickup.getPickupType()){

                case KEY:
                    if (pickup instanceof Key)
                        levelState.getPlayer().addKey((Key) pickup);
                    Log.d(TAG, collidable.getName() + " picked up a key");
                    break;
                case BOMB:
                    //add one bomb to any bomb weapons the player is carrying
                    if (((Player) collidable).getAmmoType() == AmmoType.BOMB)
                        ((Player) collidable).addAmmo(1);
                    if (((Player) collidable).getSecondaryAmmoType() == AmmoType.BOMB)
                        ((Player) collidable).addSecondaryAmmo(1);
                    Log.d(TAG, collidable.getName() + " gained a bomb");
                    break;
                case HEALTH:
                    ((Player) collidable).restoreHealth(50);
                    Log.d(TAG, collidable.getName() + " gained 50 health");
                    break;
            }
            levelState.removePickup(pickup);
        }
    }

    private void resolveExplosion(Explosion explosion, Collidable collidable){
        if (collidable instanceof Damageable){
            if (((Damageable) collidable).inflictDamage(explosion.getDamage())){
                Log.d(TAG, collidable.getName() + " has died");

                if (collidable instanceof AIEntity){
                    levelState.aiDied((AIEntity) collidable);
                }
            }
            Log.d(TAG, "explosion hit " + collidable.getName() + " and dealt " + explosion.getDamage() + " damage. " +
                    "health now at " + ((Damageable) collidable).getHealth());
        }
    }

    private void resolveProjectileHit(Projectile projectile, Collidable collidable, Vector pV){
        //if the projectile damages on contact and the collidable can take damage, damage it

        if (!(projectile instanceof Bomb)) {

            if (collidable instanceof Damageable) {
                //only damage the collidable if the projectile doesn't belong to them
                if (!projectile.getOwner().equals(collidable.getName()) && ((Damageable) collidable).inflictDamage(projectile.getDamage())) {
                    Log.d(TAG, collidable.getName() + " has died");

                    if (collidable instanceof AIEntity) {
                        levelState.aiDied((AIEntity) collidable);
                    }
                } else {
                    Log.d(TAG, collidable.getName() + " hit by projectile. health now at " + ((Damageable) collidable).getHealth());
                }
            }

            if (!projectile.getOwner().equals(collidable.getName())) {
                levelState.getBullets().remove(projectile);

                /*moveEntityCenter(projectile, pV.getRelativeToTailPoint());
                projectile.setRequestedMovementVector(reflectVectorAcrossPushVector(projectile.getRequestedMovementVector(), pV));*/
            }


        }

    }

    private void resolveDoorFieldActivation(DoorField doorField, Collidable collidable){
        Door fieldOwner = levelState.getMap().getDoors().get(doorField.getOwner());

        if (fieldOwner != null && collidable instanceof Player){
            //if it's locked and the player has the key, then unlock it
            if (fieldOwner.isLocked()){
                for (Key key : ((Player) collidable).getKeys()){

                    if (key.getUnlocks().equals(fieldOwner.getName())) {
                        fieldOwner.setLocked(false);
                        break;
                    }
                }
            }
        }

        if (fieldOwner != null && fieldOwner.isUnlocked()) {
            //if the interaction field belongs to a door, and it's unlocked
            doorsToOpen.add(fieldOwner.getName());
        }
    }

    private void resolveSolidsCollision(Collidable firstCollidable, Collidable secondCollidable, Vector pushVector, List<Collidable> bin){
        Point newCenter;

        //both entities are solid and the collision needs to be resolved.
        if (firstCollidable.canMove() && secondCollidable.canMove()) {

            //amount added to the entity's center to allow reverting to previous state
            Point firstAmountMoved;
            Point secondAmountMoved;

            //update position of first entity with half of the push vector
            firstAmountMoved = pushVector.sMult(0.5f).getRelativeToTailPoint();
            newCenter = firstCollidable.getCenter().add(firstAmountMoved);
            firstCollidable.setCenter(newCenter);

            //update position of second entity with half of the inverted pushVector
            //i.e pushes second entity away from first
            secondAmountMoved = pushVector.sMult(-0.5f).getRelativeToTailPoint();
            newCenter = secondCollidable.getCenter().add(secondAmountMoved);
            secondCollidable.setCenter(newCenter);

            restitutionCollision((Entity) firstCollidable, (Entity) secondCollidable, 0.2f);

        } else {


            MoveableEntity moveableCollidable;

            Collidable staticCollidable;

            if (firstCollidable.canMove()) {
                moveableCollidable = (MoveableEntity) firstCollidable;
                staticCollidable = secondCollidable;

            } else {
                moveableCollidable = (MoveableEntity) secondCollidable;
                staticCollidable = firstCollidable;

                pushVector = pushVector.sMult(-1f);
            }

            Point amountToMove = pushVector.getRelativeToTailPoint();
            newCenter = moveableCollidable.getCenter().add(amountToMove);
            moveableCollidable.setCenter(newCenter);

            //bigger the angle, smaller the force
            float angle = Vector.calculateAngleBetweenVectors(moveableCollidable.getVelocity().sMult(-1), pushVector);
            float prop = 1 - (angle / (float)Math.PI);

            Log.d("TANK", "prop: " + prop + "push vector: " + pushVector.relativeToString());

            moveableCollidable.addForce(pushVector.setLength(100).sMult(prop));

        }
    }

    public static float getWiggleRoom(Player player, AIEntity aiEntity){
        Point[] playerVertices = player.getCollisionVertices();

        Vector aToP = new Vector(aiEntity.getCenter(), player.getCenter());
        Vector normal = aToP.rotateAntiClockwise90().getUnitVector();

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        Point minPoint = playerVertices[0];
        Point maxPoint = playerVertices[0];

        for (Point p : playerVertices){
            float currentLength = projectOntoAxis(normal, p).first;

            if (currentLength < min) {
                minPoint = p;
                min = currentLength;
            }
            if (currentLength > max){
                maxPoint = p;
                max = currentLength;
            }
        }

        Vector aToMin = new Vector (aiEntity.getCenter(), minPoint);
        Vector aToMax = new Vector (aiEntity.getCenter(), maxPoint);

        float minAngle = Math.abs(Vector.calculateAngleBetweenVectors(aToP, aToMin));
        float maxAngle = Math.abs(Vector.calculateAngleBetweenVectors(aToP, aToMax));

        return Math.min(minAngle, maxAngle);
    }

    private void restitutionCollision(Entity a, Entity b, float cor){
        Vector ua = a.getVelocity();
        Vector ub = b.getVelocity();

        Vector uaAddub = ua.vAdd(ub);

        Vector va = uaAddub.vAdd(ub.vSub(ua)).sMult(0.5f);
        Vector vb = uaAddub.vAdd(ua.vSub(ub)).sMult(0.5f);

        a.setVelocity(va);
        b.setVelocity(vb);
    }

    private void changeVelocityForBounce(Collidable collidable, Vector pushVector){

        if (collidable instanceof MoveableEntity){
            Vector normal = pushVector.rotateAntiClockwise90().getUnitVector();
            Vector vel = ((MoveableEntity) collidable).getVelocity();

            float dot = pushVector.dot(vel);
            Vector newV = pushVector.sMult(-1 * dot * 0.5f);

            Vector v = reflectVectorAcrossPushVector(vel, pushVector);

            Log.d("VEL", "dot: " + dot + ", vel: " + vel.relativeToString() + ", norm: " + normal.relativeToString() + ", newV: " + newV.relativeToString());

            //((MoveableEntity) collidable).setVelocity(newV);

            ((MoveableEntity) collidable).setVelocity(v);
            //((MoveableEntity) collidable).addForce(v);
        }
    }

    //checks that two circles are not intersecting by checking the distance between radii,
    //returns the overlap or the empty vector (if they dont overlap)
    public static Vector collisionCheckTwoCircles(Circle circle1, Circle circle2){

        Vector center2ToCenter1 = new Vector(circle2.getCenter(), circle1.getCenter());
        float distanceBetweenCenters = center2ToCenter1.getLength();
        float radiiSum = circle1.getRadius() + circle2.getRadius();

        if (distanceBetweenCenters < radiiSum){
            return center2ToCenter1.getUnitVector().sMult(radiiSum - distanceBetweenCenters + PUSH_VECTOR_ERROR);
        }
        return Vector.ZERO_VECTOR;
    }

    private static Vector collisionCheckCircleAndPolygon(Circle circle, Collidable polygon){
        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon.getShapeIdentifier(), polygon.getCollisionVertices()));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        Vector pushVector;
        ArrayList<Vector> pushVectors = new ArrayList<>();
        for (Vector axis : orthogonalAxes){
            pushVector = isSeparatingAxis(axis, polygon.getCollisionVertices(),
                    getCircleVerticesForAxis(axis, circle.getCenter(), circle.getRadius()));

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                return Vector.ZERO_VECTOR;
            }
            pushVectors.add(pushVector);
        }

        return getMinimumPushVector(pushVectors, circle.getCenter(), polygon.getCenter());
    }

    private void aiSight(){
        for (AIEntity aiEntity : levelState.getAliveAIEntities()){

            Vector ray = new Vector(aiEntity.getCenter(), levelState.getPlayer().getCenter());
            boolean blocked;
            boolean friendlyBlocking;

            if (ray.getLength() < 1500){
                blocked = false;
                friendlyBlocking = false;

                for (Wall wall : levelState.getMap().getWalls()){
                    if (collisionCheckRay(wall, ray)){
                        blocked = true;
                        break;
                    }

                }

                if (!blocked) {
                    for (Door door : levelState.getMap().getDoors().values()) {
                        if (!door.isOpen()) {
                            if (collisionCheckRay(door, ray)) {
                                blocked = true;
                                break;
                            }
                        }
                    }
                }

                if (!blocked){

                    for (AIEntity entity : levelState.getAliveAIEntities()){
                        if (!entity.equals(aiEntity)){
                            if (collisionCheckRay(entity, ray)){
                                friendlyBlocking = true;
                                break;
                            }
                        }
                    }

                    if (levelState.getCore() != null && collisionCheckRay(levelState.getCore(), ray)){
                        friendlyBlocking = true;
                    }
                }


            } else {
                blocked = true;
                friendlyBlocking = false;
            }

            Log.d("SIGHT", "vector: " + ray + ", blocked: " + blocked);
            aiEntity.getContext().addPair(SIGHT_VECTOR, ray);
            aiEntity.getContext().addPair(SIGHT_BLOCKED, blocked);
            aiEntity.getContext().addPair(FRIENDLY_BLOCKING_SIGHT, friendlyBlocking);
        }
    }

    public Vector getPathAroundObstacle(AIEntity entity, Point end){

        Collidable closestCollidable = null;
        int smallestDistance = Integer.MAX_VALUE;

        Vector fUnit = entity.getForwardUnitVector();
        Vector steeringAxis = fUnit.rotateAntiClockwise90();

        float height = 300f;
        Point center = fUnit.sMult(height).getHead();

        Rectangle rect = new Rectangle(center, entity.getWidth()*1.5f, height, Color.GRAY);
        Point[] rectVertices = rect.getVertices();

        Vector pV;

        for (Collidable collidable : levelState.getAvoidables()){

            if (collidable.getName().equals(entity.getName()))
                continue;

            if (collidable instanceof MoveableEntity){

                int distance = euclideanHeuristic(entity.getCenter(), collidable.getCenter());

                pV = collisionCheckTwoPolygons(collidable.getCollisionVertices(), collidable.getCenter(), collidable.getShapeIdentifier(),
                        rectVertices, rect.getCenter(), rect.getShapeIdentifier());

                if (!pV.equals(Vector.ZERO_VECTOR) && distance < smallestDistance){
                    smallestDistance = distance;
                    closestCollidable = collidable;
                }
            }
        }

        if (closestCollidable == null)
            return Vector.ZERO_VECTOR;

        Log.d("AVOID", closestCollidable.getName() + " is in the way");

        Vector ray = new Vector(entity.getCenter(), closestCollidable.getCenter()).getUnitVector();

        float det = fUnit.det(ray);

        if (det < 0) {
            steeringAxis = steeringAxis.sMult(-1f);
        }

        return steeringAxis;
    }

    public static Point nearestPointOnCurve(Point position, List<Point> path, float step){
        int controlPointIdx = getClosestPointOnPathIdx(position, path);

        if (path.size() < 3){
            throw new IllegalArgumentException("given basePath is not a curve. Only has length: " + path.size());
        }

        Point p0;
        Point p1;
        Point p2;

        if (controlPointIdx == 0){

            p0 = path.get(controlPointIdx);
            p1 = path.get(controlPointIdx+1);
            p2 = path.get(controlPointIdx+2);
        } else if (controlPointIdx == path.size()-1){

            p0 = path.get(controlPointIdx-2);
            p1 = path.get(controlPointIdx-1);
            p2 = path.get(controlPointIdx);
        } else {

            p0 = path.get(controlPointIdx-1);
            p1 = path.get(controlPointIdx);
            p2 = path.get(controlPointIdx+1);
        }

        //treat controlPointIdx as the middle point on a quadratic b curve.
        QCurve curve = new QCurve(p0, p1, p2);

        if (step > 1f){
            step = 0.25f;
        }

        float t = 0f;
        Point closestPoint = null;
        int closestDistance = Integer.MAX_VALUE;

        int distance;
        Point currentPoint;
        while (Float.compare(t, 1f) < 1){

            currentPoint = curve.evalQCurve(t);
            distance = (int) new Vector(position, currentPoint).getLength();

            Log.d("CURVEE", "position: " + position +  ", currentPoint: " + currentPoint + ", distance: " + distance);

            if (distance < closestDistance){
                closestDistance = distance;
                closestPoint = currentPoint;
            }

            t += step;
        }
        return closestPoint;
    }

    public static int getClosestPointOnPathIdx(Point position, List<Point> path){

        if (path.size() > 1) {

            int smallestDistance = Integer.MAX_VALUE;
            int closestPointIdx = -1;

            for (int i = 0; i < path.size(); i++) {
                Point p = path.get(i);

                int distance = (int) new Vector(position, p).getLength();
                if (distance < smallestDistance) {
                    closestPointIdx = i;
                    smallestDistance = distance;
                }
            }

            return closestPointIdx;

        } else if (path.size() == 1) {
            return 0;
        }

        return -1;
    }

    public static Point getClosestPointOnLine(Point a, Point b, Point p){
        Vector v = new Vector(a,b);
        Vector u = new Vector(p,a);

        float t = -1 * (u.dot(v))/(v.dot(v));

        if (t > 0 && t < 1){
            return a.sMult(1-t).add(b.sMult(t));
        }

        return gt(a,b,p,0) < gt(a,b,p,1) ? a : b;
    }

    private static float gt(Point a, Point b, Point p, float t){
        return (float) Math.pow(new Vector(p, a.sMult(1-t).add(b.sMult(t))).getLength(), 2);
    }

    public Vector movingTargetAvoidanceForTanks(AIEntity controlled){

        Entity priorityEntity = levelState.getPlayer();

        float t = unalignedCollisionAvoidance(controlled, levelState.getPlayer());

        float smallestT = Float.MAX_VALUE;

        if (t > 0 && smallestT > t){
            smallestT = t;
        }

        for (AIEntity entity : levelState.getAliveAIEntities()){
            if (entity.getName().equals(controlled.getName()))
                continue;

            t = unalignedCollisionAvoidance(controlled, entity);

            if (t < 0)
                continue;

            if (smallestT > t){
                smallestT = t;
                priorityEntity = entity;
            }
        }

        Log.d("AAA", "t: " + smallestT + " priority: " + priorityEntity.getName());
        return movingObjectAvoidanceSteering(controlled, priorityEntity, smallestT);
    }

    private static float unalignedCollisionAvoidance(AIEntity controlled, Entity entity){
        Vector cVel = controlled.getVelocity();
        Vector eVel = entity.getVelocity();

        Vector relVel = eVel.vSub(cVel);
        float relSpeed = relVel.getLength();

        relVel = relVel.getUnitVector();

        Vector relPos = new Vector(entity.getCenter(), controlled.getCenter());

        float proj = relVel.dot(relPos);

        if (relSpeed < 2)
            return -1;

        return proj / relSpeed;
    }

    private static Vector movingObjectAvoidanceSteering(Entity entity1, Entity entity2, float t){
        Point cFuturePos = entity1.getCenter().add(entity1.getVelocity().sMult(t).getRelativeToTailPoint());
        Point eFuturePos = entity2.getCenter().add(entity2.getVelocity().sMult(t).getRelativeToTailPoint());

        Vector v = new Vector(eFuturePos, cFuturePos);

        Log.d("AAA", v.relativeToString() + ", length: " + v.getLength());

        if (Float.isNaN(v.getLength()) || v.getLength() > 150)
            return Vector.ZERO_VECTOR;

        return v;
    }

    private static Node<Point> mapToNearestNode(Point point, Graph<Point> graph){
        Node<Point> nearest = null;
        int distance = Integer.MAX_VALUE;

        for (Node<Point> node : graph.getNodes()){
            if (euclideanHeuristic(node.getNodeID(), point) < distance){
                nearest = node;
            }
        }

        return nearest;
    }

    public static int euclideanHeuristic(Point point, Point point1){
        return (int) new Vector(point, point1).getLength();
    }

    private static boolean collisionCheckRay(Collidable collidable, Vector ray){
        Point[] rayVertices = new Point[]{ray.getTail(), ray.getHead()};

        Vector[] axes = getEdgeNormals(getEdges(collidable.getShapeIdentifier(), collidable.getCollisionVertices()));
        List<Vector> pVs = applySAT(axes, collidable.getCollisionVertices(), rayVertices);

        //if length is equal then sight is blocked...
        return pVs.size() == axes.length;
    }

    //Separating axis theorem for two polygons
    //returns the overlap or the empty vector
    public static Vector collisionCheckTwoPolygonalCollidables(Collidable polygon1, Collidable polygon2){
        return collisionCheckTwoPolygons(polygon1.getCollisionVertices(), polygon1.getCenter(), polygon1.getShapeIdentifier(),
                polygon2.getCollisionVertices(), polygon2.getCenter(), polygon2.getShapeIdentifier());
    }

    //Separating axis theorem for two polygons
    //returns the overlap or the empty vector
    public static Vector collisionCheckTwoPolygons(Point[] firstEntityVertices, Point firstCenter, ShapeIdentifier firstShape,
                                                   Point[] secondEntityVertices, Point secondCenter, ShapeIdentifier secondShape){

        ArrayList<Vector> edges = new ArrayList<>(getEdges(firstShape, firstEntityVertices));
        edges.addAll(getEdges(secondShape, secondEntityVertices));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        List<Vector> pushVectors = applySAT(orthogonalAxes, firstEntityVertices, secondEntityVertices);

        if (pushVectors.size() == orthogonalAxes.length) {
            return getMinimumPushVector(pushVectors, firstCenter, secondCenter);
        }
        return Vector.ZERO_VECTOR;
    }

    private static List<Vector> applySAT(Vector[] axes, Point[] firstEntityVertices, Point[] secondEntityVertices){
        Vector pushVector;
        List<Vector> pushVectors = new ArrayList<>();

        for (Vector axis : axes){
            pushVector = isSeparatingAxis(axis, firstEntityVertices, secondEntityVertices);

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                break;
            } else {
                pushVectors.add(pushVector);
            }
        }

        return pushVectors;
    }

    private static Vector reflectVectorAcrossPushVector(Vector initialVector, Vector pushVector){
        pushVector = pushVector.getUnitVector();
        float dot = pushVector.dot(initialVector);

        return initialVector.vSub(pushVector.sMult(dot*2));
    }

    private static Point[] getCircleVerticesForAxis(Vector axis, Point center, float radius){

        Vector radiusVector = axis.sMult(radius);

        return new Point[]{center.add(radiusVector.getRelativeToTailPoint()),
                center.add(radiusVector.sMult(-1f).getRelativeToTailPoint())};
    }

    private static void moveEntityCenter(Collidable entity, Point amountToMove){
        Point newCenter = entity.getCenter().add(amountToMove);
        entity.setCenter(newCenter);
    }

    private void addCheckedPairNames(Collidable entity1, Collidable entity2){
        checkedPairNames.add(entity1.getName() + entity2.getName());
        checkedPairNames.add(entity2.getName() + entity1.getName());
    }

    private static Vector getMinimumPushVector(List<Vector> pushVectors, Point center1, Point center2){
        Vector minPushVector = Vector.ZERO_VECTOR;
        if (pushVectors.size() > 0) {
            minPushVector = pushVectors.get(0);

            for (Vector pushVector : pushVectors) {
                minPushVector = minPushVector.getLength() < pushVector.getLength() ? minPushVector : pushVector;
            }
        }

        if (minPushVector.dot(new Vector(center1, center2)) > 0) {
            minPushVector = minPushVector.sMult(-1f);
        }
        return minPushVector;
    }

    private static Vector[] convertToVectorsFromOrigin(Point[] vertices){
        Vector[] verticesVectors = new Vector[vertices.length];
        for (int i = 0; i < vertices.length; i++){
            verticesVectors[i] = new Vector(vertices[i]);
        }
        return verticesVectors;
    }

    //checks if the max and min points on the given direction axis overlap, returns the overlap
    //this function is the variation for checking TWO polygons
    private static Vector isSeparatingAxis(Vector axis, Point[] firstEntityVertices, Point[] secondEntityVertices){
        Pair<Float, Float> minMaxResult = projectOntoAxis(axis, firstEntityVertices);

        float firstEntityMinLength = minMaxResult.first;
        float firstEntityMaxLength = minMaxResult.second;

        minMaxResult = projectOntoAxis(axis, secondEntityVertices);
        float secondEntityMinLength = minMaxResult.first;
        float secondEntityMaxLength = minMaxResult.second;

        if (firstEntityMaxLength >= secondEntityMinLength && secondEntityMaxLength >= firstEntityMinLength) {
            float pushVectorLength = Math.min((secondEntityMaxLength - firstEntityMinLength), (firstEntityMaxLength - secondEntityMinLength));

            //push a bit more than needed so they dont overlap in future tests to compensate for float precision error
            pushVectorLength += PUSH_VECTOR_ERROR;

            return axis.getUnitVector().sMult(pushVectorLength);
        }
        return Vector.ZERO_VECTOR;
    }

    private static Pair<Float, Float> projectOntoAxis(Vector axis, Point... vertices){
        float minLength = Float.POSITIVE_INFINITY;
        float maxLength = Float.NEGATIVE_INFINITY;

        float projection;

        for (Vector vertexVector : convertToVectorsFromOrigin(vertices)){
            projection = vertexVector.dot(axis);

            minLength = Math.min(minLength, projection);
            maxLength = Math.max(maxLength, projection);
        }

        return new Pair<>(minLength, maxLength);
    }

    private static Pair<Point, Point> getMinMaxPointsForAxis(Vector axis, Point... vertices){
        float minLength = Float.POSITIVE_INFINITY;
        float maxLength = Float.NEGATIVE_INFINITY;

        Point maxVertex = new Point();
        Point minVertex = new Point();

        float projection;

        for (Vector vertexVector : convertToVectorsFromOrigin(vertices)){
            projection = vertexVector.dot(axis);

            if (projection < minLength){
                minVertex = vertexVector.getHead();
                minLength = projection;
            }

            if (projection > maxLength){
                maxVertex = vertexVector.getHead();
                maxLength = projection;
            }
        }

        return new Pair<>(minVertex, maxVertex);
    }

    //Returns the unit normals for the given edges.
    //Rotated anticlockwise, so assumes the edges given cycle clockwise around a shape
    private static Vector[] getEdgeNormals(ArrayList<Vector> edges){
        Vector[] orthogonals = new Vector[edges.size()];

        for (int i = 0; i < edges.size(); i++){
            orthogonals[i] = getEdgeNormal(edges.get(i));
        }
        return orthogonals;
    }

    private static Vector getEdgeNormal(Vector edge){
        return edge.rotateAntiClockwise90().getUnitVector();
    }

    //joins the vertices together to form edge vectors.
    //joins the last and the first vertex to form a closed shape
    //if the shape is a rectangle, only the orthogonal edges are returned
    private static ArrayList<Vector> getEdges(ShapeIdentifier shapeIdentifier, Point[] vertices){
        ArrayList<Vector> edges = new ArrayList<>();

        if (shapeIdentifier != RECTANGLE) {

            for (int i = 0; i < vertices.length - 1; i++) {
                edges.add(new Vector(vertices[i], vertices[i + 1]));
            }
            edges.add(new Vector(vertices[vertices.length - 1], vertices[0]));
        } else {
            edges.add(new Vector(vertices[0], vertices[1]));
            edges.add(new Vector(vertices[1], vertices[2]));
        }

        return edges;
    }
}