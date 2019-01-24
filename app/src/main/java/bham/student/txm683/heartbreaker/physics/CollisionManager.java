package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CollisionManager {

    private static final float PUSH_VECTOR_ERROR = 0.001f;

    private Grid broadPhaseGrid;

    private LevelState levelState;

    public int collisionCount;
    private String TAG = "hb::CollisionManager" + collisionCount;

    private ArrayList<ArrayList<Entity>> bins;

    private HashMap<Entity, Vector> pushVectorMap;

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;
        this.collisionCount = 0;
    }

    private void countCollision(){
        collisionCount++;
        TAG = "hb::CollisionManager" + collisionCount;
    }

    private void addToPushVectorInMap(Entity entity, Vector pushVector){
        Vector resultantPushVector;

        if (pushVectorMap.containsKey(entity)){
            resultantPushVector = pushVectorMap.get(entity).vAdd(pushVector);
            Log.d(TAG, "RESULT:" + resultantPushVector.relativeToString());
        } else {
            resultantPushVector = pushVector;
        }
        pushVectorMap.put(entity, resultantPushVector);
    }

    public void checkCollisions(){
        applySpatialPartitioning();

        fineGrainCollisionDetection(bins);

        for (Entity entity : pushVectorMap.keySet()){
            Vector pushVector = pushVectorMap.get(entity);

            if (pushVector != null && !pushVector.equals(new Vector())) {
                Log.d(TAG, entity.getName() + " resultantPushVector: " + pushVector.relativeToString());

                Point newCenter = entity.getShape().getCenter().add(pushVector.getRelativeToTailPoint());
                entity.getShape().setCenter(newCenter);

                entity.setPushVector(pushVector);

                countCollision();
            }

            entity.setCollided(true);
        }
    }

    /**
     * Inserts each vertex of every entity into their corresponding position in a grid,
     * returns the cells containing 2 or more entities for more in depth countCollision checks.
     */
    private void applySpatialPartitioning(){
        Point gridMaximum = new Point(levelState.getMap().getDimensions().first, levelState.getMap().getDimensions().second);

        //initialise empty grid
        int cellSize = levelState.getMap().getTileSize() * 2;
        broadPhaseGrid = new Grid(new Point(), gridMaximum, cellSize);


        broadPhaseGrid.addEntityToGrid(levelState.getPlayer());

        for (Entity entity : levelState.getStaticEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
        }

        for (Entity entity : levelState.getEnemyEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
        }

        //each element will be a bin from a grid reference with more than one entity in
        bins = new ArrayList<>();

        //fetch the bins that have more than one entity in them for the next stage of collision detection
        for (Integer column : broadPhaseGrid.getColumnKeySet()){
            for (Integer row : broadPhaseGrid.getRowKeySet(column)){
                ArrayList<Entity> bin = broadPhaseGrid.getBin(column, row);

                if (bin.size() > 1){
                    bins.add(bin);
                }
            }
        }
    }

    private void fineGrainCollisionDetection(ArrayList<ArrayList<Entity>> bins){
        //Log.d(TAG+collisionCount, "STARTING SEP AXIS THM");
        HashSet<String> checkedPairNames = new HashSet<>();

        pushVectorMap = new HashMap<>();

        for (ArrayList<Entity> bin : bins){
            if (bin.size() > 1){

                StringBuilder sb = new StringBuilder();
                for (Entity e : bin){
                    sb.append(e.getName());
                    sb.append(", ");
                }
                //Log.d(TAG+collisionCount, "entities in bin: " + sb.toString());

                //check each pair of entities in current bin
                for (int i = 0; i < bin.size() - 1; i++){
                    for (int j = i+1; j < bin.size(); j++){

                        Entity firstEntity = bin.get(i);
                        Entity secondEntity = bin.get(j);

                        if (!firstEntity.canMove() && !secondEntity.canMove()){
                            continue;
                        }

                        if (checkedPairNames.contains(firstEntity.getName()+secondEntity.getName())){
                            //Log.d(TAG+collisionCount, "already collided: " + firstEntity.getName() + ", " + secondEntity.getName());
                            continue;
                        } else {
                            checkedPairNames.add(firstEntity.getName() + secondEntity.getName());
                            checkedPairNames.add(secondEntity.getName() + firstEntity.getName());
                        }

                        Vector pushVector;
                        ShapeIdentifier firstEntityIdentifier = firstEntity.getShape().getShapeIdentifier();
                        ShapeIdentifier secondEntityIdentifier = secondEntity.getShape().getShapeIdentifier();

                        Log.d(TAG, "first entity: " + firstEntity.getName() + ", second entity: " + secondEntity.getName());

                        if (firstEntityIdentifier == ShapeIdentifier.CIRCLE && secondEntityIdentifier == ShapeIdentifier.CIRCLE){
                            pushVector = collisionCheckTwoCircles((Circle) firstEntity.getShape(), (Circle) secondEntity.getShape());

                        } else if (firstEntityIdentifier == ShapeIdentifier.CIRCLE){
                            pushVector = collisionCheckCircleAndPolygon((Circle) firstEntity.getShape(), (Polygon) secondEntity.getShape());

                        } else if (secondEntityIdentifier == ShapeIdentifier.CIRCLE){
                            //inverted so that first entity is always the one pushed away
                            pushVector = collisionCheckCircleAndPolygon((Circle) secondEntity.getShape(), (Polygon) firstEntity.getShape());
                            pushVector = pushVector.sMult(-1f);
                        } else {
                            pushVector = collisionCheckTwoPolygons((Polygon) firstEntity.getShape(), (Polygon) secondEntity.getShape());
                        }
                        //Log.d(TAG, "push vector: " + pushVector.relativeToString());

                        if (!pushVector.equals(new Vector())) {
                            //Log.d(TAG+collisionCount, "collided pair added");
                            if (firstEntity.canMove() && secondEntity.canMove()){
                                addToPushVectorInMap(firstEntity, pushVector.sMult(0.5f));
                                addToPushVectorInMap(secondEntity, pushVector.sMult(-0.5f));
                            } else if (firstEntity.canMove() && !secondEntity.canMove()){
                                addToPushVectorInMap(firstEntity, pushVector);
                                addToPushVectorInMap(secondEntity, new Vector());
                            } else {
                                addToPushVectorInMap(firstEntity, new Vector());
                                addToPushVectorInMap(secondEntity, pushVector.sMult(-1f));
                            }
                        }
                    }
                }
            }
        }
    }

    private Vector getMinimumPushVector(ArrayList<Vector> pushVectors){
        Vector minPushVector = new Vector();
        if (pushVectors.size() > 0) {
            minPushVector = pushVectors.get(0);

            for (Vector pushVector : pushVectors) {
                minPushVector = minPushVector.getLength() < pushVector.getLength() ? minPushVector : pushVector;
            }
        }
        return minPushVector;
    }

    private Vector[] convertToVectorsFromOrigin(Point[] vertices){
        Vector[] verticesVectors = new Vector[vertices.length];
        for (int i = 0; i < vertices.length; i++){
            verticesVectors[i] = new Vector(vertices[i]);
        }
        return verticesVectors;
    }

    private Vector isSeparatingAxis(Vector axis, Vector[] firstEntityVertices, Vector[] secondEntityVertices){
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

    private Vector isSeparatingAxis(Vector axis, Vector nearSidePoint, Vector[] secondEntityVertices){
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



    private Vector[] getOrthogonals(ArrayList<Vector> edges){
        Vector[] orthogonals = new Vector[edges.size()];

        for (int i = 0; i < edges.size(); i++){
            orthogonals[i] = edges.get(i).rotateAntiClockwise90().getUnitVector();
        }
        return orthogonals;
    }

    private ArrayList<Vector> getEdges(ShapeIdentifier shapeIdentifier, Point[] vertices){
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

    private Vector collisionCheckTwoPolygons(Polygon polygon1, Polygon polygon2){
        Point[] firstEntityVertices = polygon1.getVertices();
        Point[] secondEntityVertices = polygon2.getVertices();

        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon1.getShapeIdentifier(), firstEntityVertices));
        edges.addAll(getEdges(polygon2.getShapeIdentifier(), secondEntityVertices));

        Vector[] orthogonalAxes = getOrthogonals(edges);

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

    private Vector collisionCheckTwoCircles(Circle circle1, Circle circle2){
        Vector center2ToCenter1 = new Vector(circle2.getCenter(), circle1.getCenter());
        float distanceBetweenCenters = center2ToCenter1.getLength();
        float radiiSum = circle1.getRadius() + circle2.getRadius();
        if (distanceBetweenCenters < radiiSum){
            return center2ToCenter1.getUnitVector().sMult(radiiSum - distanceBetweenCenters + PUSH_VECTOR_ERROR);
        }
        return new Vector();
    }

    private Vector collisionCheckCircleAndPolygon(Circle circle, Polygon polygon){
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
        Vector[] orthogonals = getOrthogonals(getEdges(polygon.getShapeIdentifier(), polygon.getCollisionVertices()));

        int minOrthIndex = -1;
        float maxOrthDot = -1;

        //find the normal vector with the smallest angle between itself and the vectorBetweenCenters
        for (int i = 0; i < orthogonals.length; i++){
            float currentOrthDot = orthogonals[i].dot(vectorBetweenCenters);
            Log.d(TAG, "orth " + i + ": " + orthogonals[i].relativeToString());
            Log.d(TAG, "orth dot " + i + ": " + currentOrthDot);

            if (maxOrthDot < currentOrthDot){
                maxOrthDot = currentOrthDot;
                minOrthIndex = i;
            }
        }

        Vector pushVector;

        Vector[] polygonVerticesFromOrigin = convertToVectorsFromOrigin(polygon.getCollisionVertices());

        pushVector = isSeparatingAxis(vectorBetweenCenters, nearSideRadius, polygonVerticesFromOrigin);

        Log.d(TAG, "pushVector: " + pushVector.relativeToString() + ", max orth index: " + minOrthIndex);

        if (minOrthIndex >= 0 && !pushVector.equals(new Vector())){
                Vector secondaryPushVector = isSeparatingAxis(orthogonals[minOrthIndex],
                        new Vector(circle.getCenter().add(orthogonals[minOrthIndex].sMult(-1f*circle.getRadius()).getRelativeToTailPoint())),
                        polygonVerticesFromOrigin);

                Log.d(TAG, "secondary push: " + secondaryPushVector.relativeToString());
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