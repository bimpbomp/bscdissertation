package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.EntityShape;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashSet;

public class CollisionManager {

    private static final float PUSH_VECTOR_ERROR = 0.001f;

    private Grid broadPhaseGrid;

    private LevelState levelState;

    private HashSet<String> checkedPairNames;

    private HashSet<String> currentTickCollidedPairs;

    private HashSet<String> collidedLastTick;

    public int collisionCount;
    private String TAG = "hb::CollisionManager";

    private ArrayList<Pair<ArrayList<Entity>, Pair<Integer,Integer>>> bins;

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;
        this.collisionCount = 0;

        this.collidedLastTick = new HashSet<>();
    }

    private void addToCollidedThisTickSet(Entity entity1, Entity entity2){
        if (!collidedLastTick.contains(entity1.getName()+entity2.getName())) {
            collisionCount++;
            TAG = "hb::CollisionManager" + collisionCount;
        }
        currentTickCollidedPairs.add(entity1.getName() + entity2.getName());
        currentTickCollidedPairs.add(entity2.getName() + entity1.getName());
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

        broadPhaseGrid.addEntityToGrid(levelState.getPlayer());

        for (Entity entity : levelState.getEnemyEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
        }

        for (Entity entity : levelState.getStaticEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
        }

        //each element will be a bin from a grid reference with more than one entity in
        bins = new ArrayList<>();

        //fetch the bins that have more than one entity in them for the next stage of collision detection
        for (Integer column : broadPhaseGrid.getColumnKeySet()){
            for (Integer row : broadPhaseGrid.getRowKeySet(column)){
                Pair<ArrayList<Entity>, Pair<Integer, Integer>> bin = new Pair<>(broadPhaseGrid.getBin(column, row), new Pair<>(column, row));

                if (bin.first.size() > 1){
                    bins.add(bin);
                }
            }
        }
    }

    private void fineGrainCollisionDetection(ArrayList<Pair<ArrayList<Entity>, Pair<Integer, Integer>>> bins){
        //Log.d(TAG+collisionCount, "STARTING SEP AXIS THM");
        checkedPairNames = new HashSet<>();
        currentTickCollidedPairs = new HashSet<>();

        ArrayList<MoveableEntity> deadEntities = new ArrayList<>();

        for (Pair<ArrayList<Entity>, Pair<Integer, Integer>> binPair : bins){
            ArrayList<Entity> bin = binPair.first;
            Pair<Integer, Integer> binGridReference = binPair.second;

            if (bin.size() > 1){

                StringBuilder sb = new StringBuilder();
                for (Entity e : bin){
                    sb.append(e.getName());
                    sb.append(", ");
                }
                //Log.d(TAG, "entities in bin: " + sb.toString());

                //check each pair of entities in current bin
                for (int i = 0; i < bin.size() - 1; i++){
                    for (int j = i+1; j < bin.size(); j++){

                        Entity firstEntity = bin.get(i);
                        Entity secondEntity = bin.get(j);

                        //if both entities are static, skip
                        if (!firstEntity.canMove() && !secondEntity.canMove()){
                            continue;
                        }

                        //if the two entities have already been checked together for collisions, skip
                        //otherwise add them to the checked pair set.
                        if (checkedPairNames.contains(firstEntity.getName()+secondEntity.getName())){
                            //Log.d(TAG+collisionCount, "already collided: " + firstEntity.getName() + ", " + secondEntity.getName());
                            continue;
                        }
                        
                        Vector pushVector = getPushVectorBetweenTwoShapes(firstEntity.getShape(), secondEntity.getShape());


                        //if a collision has occurred (zero vector says a collision hasn't occurred)
                        if (!pushVector.equals(new Vector())) {

                            //at least one entity can move, as we ignored any pairs of static entities earlier on

                            //true if the entity doesnt collide with any statics after applying
                            //pushVector.
                            boolean firstAbleToMove;
                            boolean secondAbleToMove;

                            Point newCenter;

                            //amount added to the entity's center to allow reverting to previous state
                            Point firstAmountMoved;
                            Point secondAmountMoved;

                            if (firstEntity.canMove() && secondEntity.canMove()) {

                                if (firstEntity.hasAttacked()){
                                    Log.d(TAG, firstEntity.getName() + " has attacked");

                                    Vector firstForwardVector = firstEntity.getShape().getForwardUnitVector();

                                    float dotProduct = firstForwardVector.dot(pushVector.getUnitVector().sMult(-1f));

                                    Log.d(TAG, firstEntity.getName() + "'s forward vector has a dot product of " + dotProduct + " with the push vector");

                                    //if the second entity is within a 45 degree sector either side of the forward vector,
                                    //the attack is classed as a hit.
                                    if (dotProduct - 0.001f < 1f && dotProduct >= 0.7f){
                                        Log.d(TAG, secondEntity.getName() + " damaged by " + firstEntity.getName() + " with damage " + ((MoveableEntity) firstEntity).getDamageFromMeleeAttack());
                                         if (((MoveableEntity) secondEntity).damage(((MoveableEntity) firstEntity).getDamageFromMeleeAttack()))
                                             deadEntities.add((MoveableEntity)secondEntity);
                                    }
                                }
                                //resolve collisions with any statics that share a cell with either entity
                                isEntityAbleToBePushed(firstEntity, binGridReference, true);
                                isEntityAbleToBePushed(secondEntity, binGridReference, true);

                                //update position of first entity with half of the push vector
                                firstAmountMoved = pushVector.sMult(0.5f).getRelativeToTailPoint();
                                newCenter = firstEntity.getShape().getCenter().add(firstAmountMoved);
                                firstEntity.getShape().setCenter(newCenter);

                                //update position of second entity with half of the inverted pushVector
                                //i.e pushes second entity away from first
                                secondAmountMoved = pushVector.sMult(-0.5f).getRelativeToTailPoint();
                                newCenter = secondEntity.getShape().getCenter().add(secondAmountMoved);
                                secondEntity.getShape().setCenter(newCenter);

                                //check if the entities now overlap any statics with their new positions
                                firstAbleToMove = isEntityAbleToBePushed(firstEntity, binGridReference, false);
                                secondAbleToMove = isEntityAbleToBePushed(secondEntity, binGridReference, false);

                                if (!secondAbleToMove){
                                    //if the second entity now overlaps a static, move it back to it's original position
                                    //and move the first entity the other half of the distance so it doesn't overlap the second
                                    //collision is resolved, move to next entity pair
                                    moveEntityCenter(firstEntity, firstAmountMoved);
                                    moveEntityCenter(secondEntity, secondAmountMoved.smult(-1f));

                                } else if (!firstAbleToMove){
                                    //if the first entity now overlaps a static, move it back to it's original position
                                    //and move the second entity the other half of the distance so it doesn't overlap the first
                                    //collision is resolved, move to next entity pair
                                    moveEntityCenter(firstEntity, firstAmountMoved.smult(-1f));
                                    moveEntityCenter(secondEntity, secondAmountMoved);
                                }

                            } else if (firstEntity.canMove()){

                                //if only the first entity can move (is not static), resolution is to add all push
                                //vector to first entity
                                //collision is resolved, move to next entity pair
                                firstAmountMoved = pushVector.getRelativeToTailPoint();
                                newCenter = firstEntity.getShape().getCenter().add(firstAmountMoved);
                                firstEntity.getShape().setCenter(newCenter);

                            } else {
                                //if only the second entity can move (is not static), resolution is to add all push
                                //vector to second entity
                                //collision is resolved, move to next entity pair
                                secondAmountMoved = pushVector.sMult(-1).getRelativeToTailPoint();
                                newCenter = secondEntity.getShape().getCenter().add(secondAmountMoved);
                                secondEntity.getShape().setCenter(newCenter);
                            }
                            addToCollidedThisTickSet(firstEntity, secondEntity);

                            //handle attacks
                        }
                        //add entity names to the checked names set so that they aren't checked twice
                        addCheckedPairNames(firstEntity, secondEntity);
                    }
                }
            }
        }

        if (levelState.getPlayer().hasAttacked()) {
            levelState.getPlayer().resetAttack();
        }
        if (levelState.getPlayer().getAttackCooldown() > 0)
            levelState.getPlayer().tickCooldown();

        for (MoveableEntity entity : levelState.getEnemyEntities()){
            if (entity.hasAttacked())
                entity.resetAttack();

            if (entity.getAttackCooldown() > 0)
                entity.tickCooldown();
        }

        for (MoveableEntity entity : deadEntities){
            levelState.removeEnemy(entity);
        }

        collidedLastTick = currentTickCollidedPairs;
    }

    private static void moveEntityCenter(Entity entity, Point amountToMove){
        Point newCenter = entity.getShape().getCenter().add(amountToMove);
        entity.getShape().setCenter(newCenter);
    }

    public static Vector getPushVectorBetweenTwoShapes(EntityShape shape1, EntityShape shape2){
        Vector pushVector;

        ShapeIdentifier firstEntityIdentifier = shape1.getShapeIdentifier();
        ShapeIdentifier secondEntityIdentifier = shape2.getShapeIdentifier();

        //collision detection method varies depending on shape combination
        if (firstEntityIdentifier == ShapeIdentifier.CIRCLE && secondEntityIdentifier == ShapeIdentifier.CIRCLE){
            pushVector = collisionCheckTwoCircles((Circle) shape1, (Circle) shape2);

        } else if (firstEntityIdentifier == ShapeIdentifier.CIRCLE){
            pushVector = collisionCheckCircleAndPolygon((Circle) shape1, (Polygon) shape2);

        } else if (secondEntityIdentifier == ShapeIdentifier.CIRCLE){
            //inverted so that first entity is always the one pushed away
            pushVector = collisionCheckCircleAndPolygon((Circle) shape2, (Polygon) shape1);
            pushVector = pushVector.sMult(-1f);
        } else {
            pushVector = collisionCheckTwoPolygons((Polygon) shape1, (Polygon) shape2);
        }
        return pushVector;
    }

    private void addCheckedPairNames(Entity entity1, Entity entity2){
        checkedPairNames.add(entity1.getName() + entity2.getName());
        checkedPairNames.add(entity2.getName() + entity1.getName());
    }
    
    private boolean isEntityAbleToBePushed(Entity entity, Pair<Integer, Integer> binReference, boolean resolveCollision){

        //look at adjacent cells in grid that the entity exists in (if they exist).
        for (int i = binReference.first - 1; i < binReference.first + 2; i++){
            for (int j = binReference.second - 1; j < binReference.second + 2; j++){

                ArrayList<Entity> bin = broadPhaseGrid.getBin(i, j);
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
    private boolean isStaticCollision(Entity entity, ArrayList<Entity> bin, boolean resolveCollision){
        boolean collided;
        for (Entity entityInBin : bin) {
            if (!entityInBin.canMove()) {
                //check if the two entities collide
                Vector pushVector = getPushVectorBetweenTwoShapes(entity.getShape(), entityInBin.getShape());
                collided = !(pushVector.equals(new Vector()));

                if (collided) {
                    if (resolveCollision){
                        //if the collision is meant to be resolved at this stage, resolve it and mark
                        //the entities as checked
                        addCheckedPairNames(entity, entityInBin);
                        moveEntityCenter(entity, pushVector.getRelativeToTailPoint());

                        Log.d(TAG, entity.getName() + ":" + entityInBin.getName());
                    }
                    addToCollidedThisTickSet(entity, entityInBin);
                    return true;
                }
            }
        }
        return false;
    }

    private static Vector getMinimumPushVector(ArrayList<Vector> pushVectors){
        Vector minPushVector = new Vector();
        if (pushVectors.size() > 0) {
            minPushVector = pushVectors.get(0);

            for (Vector pushVector : pushVectors) {
                minPushVector = minPushVector.getLength() < pushVector.getLength() ? minPushVector : pushVector;
            }
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
        return new Vector();
    }

    //this function is the variation for checking a polygon and a circle
    //polygon's vertices is in secondEntityVertices
    //circle's nearest point to the circle is in nearSidePoint
    //checks if the max point on the second entity and the point on the circle closest to
    //the polygon overlap, returns the overlap
    private static Vector isSeparatingAxis(Vector axis, Vector nearSidePoint, Vector[] secondEntityVertices){
        float firstEntityMaxLength = Float.NEGATIVE_INFINITY;

        float projection;
        for (Vector vertexVector : secondEntityVertices){
            projection = vertexVector.dot(axis);
            firstEntityMaxLength = Math.max(firstEntityMaxLength, projection);
        }

        if (firstEntityMaxLength > nearSidePoint.dot(axis)) {
            float pushVectorLength = firstEntityMaxLength - nearSidePoint.dot(axis);

            //push a bit more than needed so they dont overlap in future tests to compensate for float precision error
            pushVectorLength += PUSH_VECTOR_ERROR;

            return axis.sMult(pushVectorLength);
        }
        return new Vector();
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

        if (shapeIdentifier != ShapeIdentifier.RECT) {

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

    //Separating axis theorem for two polygons
    //returns the overlap or the empty vector
    public static Vector collisionCheckTwoPolygons(Polygon polygon1, Polygon polygon2){
        Point[] firstEntityVertices = polygon1.getVertices();
        Point[] secondEntityVertices = polygon2.getVertices();

        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon1.getShapeIdentifier(), firstEntityVertices));
        edges.addAll(getEdges(polygon2.getShapeIdentifier(), secondEntityVertices));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        ArrayList<Vector> pushVectors = new ArrayList<>();
        boolean collided = true;
        for (Vector axis : orthogonalAxes){
            Vector pushVector = isSeparatingAxis(axis, convertToVectorsFromOrigin(firstEntityVertices), convertToVectorsFromOrigin(secondEntityVertices));

            if (pushVector.equals(new Vector())){
                collided = false;
                break;
            } else {
                //Log.d(TAG + collisionCount, "push vector: " + pushVector.toString());
                pushVectors.add(pushVector);
            }
        }

        if (collided) {
            Vector minPushVector = getMinimumPushVector(pushVectors);

            if (minPushVector.dot(new Vector(polygon1.getCenter(), polygon2.getCenter())) > 0) {
                minPushVector = minPushVector.sMult(-1f);
            }
            return minPushVector;
        }
        return new Vector();
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
        return new Vector();
    }

    //checks if a collision has occurred between a circle and a polygon.
    //not too accurate but functional.
    //returns the overlap or the empty vector (if there is no overlap)
    public static Vector collisionCheckCircleAndPolygon(Circle circle, Polygon polygon){
        //vector from circle center to polygon center
        Vector vectorBetweenCenters = new Vector(circle.getCenter(), polygon.getCenter());
        //Point on circle circumference closest to polygon
        Point pointOnNearSideCircumference = vectorBetweenCenters.getUnitVector().sMult(circle.getRadius()).getHead();

        //vector from origin to pointOnNearSideCircumference
        Vector nearSideRadius = new Vector(pointOnNearSideCircumference);

        //get unit vector of the vector in the direction from polygon center to circle center
        //will be used as the axis for isSeparatingAxis
        vectorBetweenCenters = vectorBetweenCenters.sMult(-1f).getUnitVector();

        //normal vectors for each of the polygon's edges
        Vector[] orthogonals = getEdgeNormals(getEdges(polygon.getShapeIdentifier(), polygon.getCollisionVertices()));

        int minOrthIndex = -1;
        float maxOrthDot = -1;

        //find the normal vector with the smallest angle between itself and the vectorBetweenCenters
        for (int i = 0; i < orthogonals.length; i++){
            float currentOrthDot = orthogonals[i].dot(vectorBetweenCenters);
            /*Log.d(TAG, "orth " + i + ": " + orthogonals[i].relativeToString());
            Log.d(TAG, "orth dot " + i + ": " + currentOrthDot);*/

            if (maxOrthDot < currentOrthDot){
                maxOrthDot = currentOrthDot;
                minOrthIndex = i;
            }
        }

        Vector pushVector;

        Vector[] polygonVerticesFromOrigin = convertToVectorsFromOrigin(polygon.getCollisionVertices());

        pushVector = isSeparatingAxis(vectorBetweenCenters, nearSideRadius, polygonVerticesFromOrigin);

        //Log.d(TAG, "pushVector: " + pushVector.relativeToString() + ", max orth index: " + minOrthIndex);

        if (minOrthIndex >= 0 && !pushVector.equals(new Vector())){
                Vector secondaryPushVector = isSeparatingAxis(orthogonals[minOrthIndex],
                        new Vector(circle.getCenter().add(orthogonals[minOrthIndex].sMult(-1f*circle.getRadius()).getRelativeToTailPoint())),
                        polygonVerticesFromOrigin);

                //Log.d(TAG, "secondary push: " + secondaryPushVector.relativeToString());
                if (!secondaryPushVector.equals(new Vector())){
                    pushVector = secondaryPushVector;
                } else {
                    pushVector = new Vector();
                }
        } else {
            pushVector = new Vector();
        }
        return pushVector;
    }

    public Grid getBroadPhaseGrid() {
        return broadPhaseGrid;
    }
}