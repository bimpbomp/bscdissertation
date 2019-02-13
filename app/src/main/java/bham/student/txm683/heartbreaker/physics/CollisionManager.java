package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Bomb;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.physics.fields.DoorField;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashSet;

import static bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier.RECTANGLE;

public class CollisionManager {

    private static final float PUSH_VECTOR_ERROR = 0.001f;

    private Grid broadPhaseGrid;

    private LevelState levelState;

    private HashSet<String> checkedPairNames;
    private HashSet<String> doorsToOpen;

    private static String TAG = "hb::CollisionManager";

    private ArrayList<Pair<ArrayList<Collidable>, Pair<Integer,Integer>>> bins;

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;
    }

    public void checkCollisions(){
        applySpatialPartitioning();

        fineGrainCollisionDetection(bins);
    }

    /**
     * Inserts each vertex of every entity into their corresponding position in a grid,
     * returns the cells containing 2 or more entities for more in depth addToCollidedThisTickSet checks.
     */
    private void applySpatialPartitioning(){

        Point gridMaximum = new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight());

        //initialise empty grid
        int cellSize = levelState.getMap().getTileSize() * 2;
        broadPhaseGrid = new Grid(new Point(levelState.getMap().getTileSize()/-2f,levelState.getMap().getTileSize()/-2f), gridMaximum, cellSize);

        //add player
        broadPhaseGrid.addEntityToGrid(levelState.getPlayer());

        //add ai
        for (Collidable enemy : levelState.getEnemyEntities()){
            broadPhaseGrid.addEntityToGrid(enemy);
        }

        //add walls
        for (Collidable wall : levelState.getMap().getWalls()){
            broadPhaseGrid.addEntityToGrid(wall);
        }

        //add doors
        for (Door door : levelState.getMap().getDoors().values()){
            broadPhaseGrid.addEntityToGrid(door);
            broadPhaseGrid.addEntityToGrid(door.getPrimaryField());
            broadPhaseGrid.addEntityToGrid(door.getSecondaryField());
        }

        //add projectiles
        for (Projectile projectile : levelState.getBullets()){
            broadPhaseGrid.addProjectileToGrid(projectile);
        }

        //add explosions
        for (Explosion explosion : levelState.getExplosions()){
            broadPhaseGrid.addEntityToGrid(explosion);
        }

        //add pickups
        for (Pickup pickup : levelState.getPickups()){
            broadPhaseGrid.addEntityToGrid(pickup);
        }

        //add core
        broadPhaseGrid.addEntityToGrid(levelState.getCore());

        //each element will be a bin from a grid reference with more than one entity in
        bins = new ArrayList<>();

        //fetch the bins that have more than one entity in them for the next stage of collision detection
        for (Integer column : broadPhaseGrid.getColumnKeySet()){
            for (Integer row : broadPhaseGrid.getRowKeySet(column)){
                Pair<ArrayList<Collidable>, Pair<Integer, Integer>> bin = new Pair<>(broadPhaseGrid.getBin(column, row), new Pair<>(column, row));

                if (bin.first.size() > 1){
                    bins.add(bin);
                }
            }
        }
    }

    private void fineGrainCollisionDetection(ArrayList<Pair<ArrayList<Collidable>, Pair<Integer, Integer>>> bins){
        checkedPairNames = new HashSet<>();

        doorsToOpen = new HashSet<>();

        Collidable firstCollidable;
        Collidable secondCollidable;

        Vector pushVector;

        //iterate through bins
        for (Pair<ArrayList<Collidable>, Pair<Integer, Integer>> binPair : bins){

            ArrayList<Collidable> bin = binPair.first;
            Pair<Integer, Integer> binGridReference = binPair.second;

            if (bin.size() > 1){

                //check each pair of entities in current bin
                for (int i = 0; i < bin.size() - 1; i++){
                    for (int j = i+1; j < bin.size(); j++) {

                        firstCollidable = bin.get(i);
                        secondCollidable = bin.get(j);

                        //if both entities are static or not solid, skip
                        if ((!firstCollidable.canMove() && !secondCollidable.canMove()) ||
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
                                    resolveProjectileHit((Projectile) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof DoorField){
                                pushVector = collisionCheckTwoPolygons(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveDoorFieldActivation((DoorField) nonSolidEntity);
                                }
                            } else if (nonSolidEntity instanceof Pickup){
                                pushVector = collisionCheckTwoPolygons(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolvePickupActivation((Pickup) nonSolidEntity, solidEntity);
                                }
                            }

                        } else {
                            //both collidables are solid
                            pushVector = collisionCheckTwoPolygons(firstCollidable, secondCollidable);

                            if (!pushVector.equals(Vector.ZERO_VECTOR)) {
                                //collision occurred
                                resolveSolidsCollision(firstCollidable, secondCollidable, pushVector, binGridReference);
                            }
                        }
                        //add entity names to the checked names set so that they aren't checked twice
                        addCheckedPairNames(firstCollidable, secondCollidable);
                    }
                }
            }
        }

        for (Door door : levelState.getMap().getDoors().values()){
            if (doorsToOpen.contains(door.getName()))
                door.setOpen(true);
            else
                door.setOpen(false);
        }
    }

    private void resolvePickupActivation(Pickup pickup, Collidable collidable){
        if (collidable instanceof Player){
            switch (pickup.getPickupType()){

                case BASIC_WEAPON:
                    break;
                case BOMB:
                    //add one bomb to any bomb weapons the player is carrying
                    if (((Player) collidable).getPrimaryAmmoType() == AmmoType.BOMB)
                        ((Player) collidable).addPrimaryAmmo(1);
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

    private static void resolveExplosion(Explosion explosion, Collidable collidable){
        if (collidable instanceof Damageable){
            Log.d(TAG, "explosion damaged " + collidable.getName());
            if (((Damageable) collidable).inflictDamage(explosion.getDamage())){
                Log.d(TAG, collidable.getName() + " has died");
            }
            Log.d(TAG, "explosion hit " + collidable.getName() + " and dealt " + explosion.getDamage() + " damage. " +
                    "health now at " + ((Damageable) collidable).getHealth());
        }
    }

    private static void resolveProjectileHit(Projectile projectile, Collidable collidable){
        //if the projectile damages on contact and the collidable can take damage, damage it
        if (!(projectile instanceof Bomb) && collidable instanceof Damageable) {
            //only damage the collidable if the projectile doesn't belong to them
            if (!projectile.getOwner().equals(collidable.getName()) && ((Damageable) collidable).inflictDamage(projectile.getDamage())){
                Log.d(TAG, collidable.getName() + " has died");
            }
            Log.d(TAG, collidable.getName() + " hit by projectile. health now at " + ((Damageable) collidable).getHealth());
        }
    }

    private void resolveDoorFieldActivation(DoorField doorField){
        Door fieldOwner = levelState.getMap().getDoors().get(doorField.getOwner());

        if (fieldOwner != null && fieldOwner.isSideUnlocked(doorField.getName())) {
            //if the interaction field belongs to a door
            //isSideUnlocked it if the side the field is on is unlocked
            doorsToOpen.add(fieldOwner.getName());
        }
    }

    private void resolveSolidsCollision(Collidable firstCollidable, Collidable secondCollidable, Vector pushVector, Pair<Integer, Integer> binGridReference){
        //true if the entity doesnt collide with any statics after applying
        //pushVector.
        boolean firstAbleToMove;
        boolean secondAbleToMove;

        Point newCenter;

        //amount added to the entity's center to allow reverting to previous state
        Point firstAmountMoved;
        Point secondAmountMoved;

        //both entities are solid and the collision needs to be resolved.
        if (firstCollidable.canMove() && secondCollidable.canMove()) {

            //resolve collisions with any statics that share a cell with either entity
            isEntityAbleToBePushed(firstCollidable, binGridReference, true);
            isEntityAbleToBePushed(secondCollidable, binGridReference, true);

            //update position of first entity with half of the push vector
            firstAmountMoved = pushVector.sMult(0.5f).getRelativeToTailPoint();
            newCenter = firstCollidable.getCenter().add(firstAmountMoved);
            firstCollidable.setCenter(newCenter);

            //update position of second entity with half of the inverted pushVector
            //i.e pushes second entity away from first
            secondAmountMoved = pushVector.sMult(-0.5f).getRelativeToTailPoint();
            newCenter = secondCollidable.getCenter().add(secondAmountMoved);
            secondCollidable.setCenter(newCenter);

            //check if the entities now overlap any statics with their new positions
            firstAbleToMove = isEntityAbleToBePushed(firstCollidable, binGridReference, false);
            secondAbleToMove = isEntityAbleToBePushed(secondCollidable, binGridReference, false);

            if (!secondAbleToMove) {
                //if the second entity now overlaps a static, tick it back to it's original position
                //and tick the first entity the other half of the distance so it doesn't overlap the second
                //collision is resolved, tick to next entity pair
                moveEntityCenter(firstCollidable, firstAmountMoved);
                moveEntityCenter(secondCollidable, secondAmountMoved.sMult(-1f));

            } else if (!firstAbleToMove) {
                //if the first entity now overlaps a static, tick it back to it's original position
                //and tick the second entity the other half of the distance so it doesn't overlap the first
                //collision is resolved, tick to next entity pair
                moveEntityCenter(firstCollidable, firstAmountMoved.sMult(-1f));
                moveEntityCenter(secondCollidable, secondAmountMoved);
            }

        } else if (firstCollidable.canMove()) {

            //if only the first entity can tick (is not static), resolution is to add all push
            //vector to first entity
            //collision is resolved, tick to next entity pair
            firstAmountMoved = pushVector.getRelativeToTailPoint();
            newCenter = firstCollidable.getCenter().add(firstAmountMoved);
            firstCollidable.setCenter(newCenter);

        } else if (secondCollidable.canMove()) {
            //if only the second entity can tick (is not static), resolution is to add all push
            //vector to second entity
            //collision is resolved, tick to next entity pair
            secondAmountMoved = pushVector.sMult(-1).getRelativeToTailPoint();
            newCenter = secondCollidable.getCenter().add(secondAmountMoved);
            secondCollidable.setCenter(newCenter);
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
            pushVector = isSeparatingAxis(axis, convertToVectorsFromOrigin(polygon.getCollisionVertices()),
                    convertToVectorsFromOrigin(getCircleVerticesForAxis(axis, circle.getCenter(), circle.getRadius())));

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                return Vector.ZERO_VECTOR;
            }
            pushVectors.add(pushVector);
        }

        return getMinimumPushVector(pushVectors, circle.getCenter(), polygon.getCenter());
    }

    //Separating axis theorem for two polygons
    //returns the overlap or the empty vector
    public static Vector collisionCheckTwoPolygons(Collidable polygon1, Collidable polygon2){
        Vector pushVector;
        Point[] firstEntityVertices = polygon1.getCollisionVertices();
        Point[] secondEntityVertices = polygon2.getCollisionVertices();

        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon1.getShapeIdentifier(), firstEntityVertices));
        edges.addAll(getEdges(polygon2.getShapeIdentifier(), secondEntityVertices));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        ArrayList<Vector> pushVectors = new ArrayList<>();
        boolean collided = true;
        for (Vector axis : orthogonalAxes){
            pushVector = isSeparatingAxis(axis, convertToVectorsFromOrigin(firstEntityVertices), convertToVectorsFromOrigin(secondEntityVertices));

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                collided = false;
                break;
            } else {
                //Log.d(TAG + collisionCount, "push vector: " + pushVector.toString());
                pushVectors.add(pushVector);
            }
        }

        if (collided) {
            return getMinimumPushVector(pushVectors, polygon1.getCenter(), polygon2.getCenter());
        }
        return Vector.ZERO_VECTOR;
    }

    private static Vector reflectVectorAcrossPushVector(Vector initialVector, Vector pushVector){
        float angle = Vector.calculateAngleBetweenVectors(pushVector, initialVector);
        return initialVector.rotate((float)Math.cos(angle), (float) Math.cos(angle)).sMult(-1f);
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
    
    private boolean isEntityAbleToBePushed(Collidable entity, Pair<Integer, Integer> binReference, boolean resolveCollision){

        //look at adjacent cells in grid that the entity exists in (if they exist).
        for (int i = binReference.first - 1; i < binReference.first + 2; i++){
            for (int j = binReference.second - 1; j < binReference.second + 2; j++){

                ArrayList<Collidable> bin = broadPhaseGrid.getBin(i, j);
                //only check the grid references that the entity exists in
                if (bin != null && bin.contains(entity)){
                    //check if the entity collides with any statics in this cell
                    boolean collided = isStaticCollision(entity, bin, resolveCollision);
                    if (collided)
                        return false;
                }
            }
        }
        return true;
    }

    //checks if the given entity collides with any statics in the given bin.
    //resolveCollision  determines if a collision is resolved should one be detected or not
    private boolean isStaticCollision(Collidable entity, ArrayList<Collidable> bin, boolean resolveCollision){

        for (Collidable entityInBin : bin) {
            if (!entityInBin.canMove()) {
                //check if the two entities collide
                Vector pushVector;

                pushVector = collisionCheckTwoPolygons(entity, entityInBin);

                if (!pushVector.equals(Vector.ZERO_VECTOR)) {
                    if (resolveCollision){
                        //if the collision is meant to be resolved at this stage, resolve it and mark
                        //the entities as checked
                        moveEntityCenter(entity, pushVector.getRelativeToTailPoint());
                        addCheckedPairNames(entity, entityInBin);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static Vector getMinimumPushVector(ArrayList<Vector> pushVectors, Point center1, Point center2){
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
    private static Vector isSeparatingAxis(Vector axis, Vector[] firstEntityVertices, Vector[] secondEntityVertices){
        float firstEntityMinLength = Float.POSITIVE_INFINITY;
        float firstEntityMaxLength = Float.NEGATIVE_INFINITY;

        float secondEntityMinLength = Float.POSITIVE_INFINITY;
        float secondEntityMaxLength = Float.NEGATIVE_INFINITY;

        float projection;
        for (Vector vertexVector : firstEntityVertices){
            projection = vertexVector.dot(axis);

            firstEntityMinLength = Math.min(firstEntityMinLength, projection);
            firstEntityMaxLength = Math.max(firstEntityMaxLength, projection);
        }

        for (Vector vertexVector : secondEntityVertices){
            projection = vertexVector.dot(axis);

            secondEntityMinLength = Math.min(secondEntityMinLength, projection);
            secondEntityMaxLength = Math.max(secondEntityMaxLength, projection);
        }

        if (firstEntityMaxLength >= secondEntityMinLength && secondEntityMaxLength >= firstEntityMinLength) {
            float pushVectorLength = Math.min((secondEntityMaxLength - firstEntityMinLength), (firstEntityMaxLength - secondEntityMinLength));

            //push a bit more than needed so they dont overlap in future tests to compensate for float precision error
            pushVectorLength += PUSH_VECTOR_ERROR;

            return axis.getUnitVector().sMult(pushVectorLength);
        }
        return Vector.ZERO_VECTOR;
    }

    //Returns the unit normals for the given edges.
    //Rotated anticlockwise, so assumes the edges given cycle clockwise around a shape
    private static Vector[] getEdgeNormals(ArrayList<Vector> edges){
        Vector[] orthogonals = new Vector[edges.size()];

        for (int i = 0; i < edges.size(); i++){
            orthogonals[i] = edges.get(i).rotateAntiClockwise90().getUnitVector();
        }
        return orthogonals;
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

    public Grid getBroadPhaseGrid() {
        return broadPhaseGrid;
    }
}