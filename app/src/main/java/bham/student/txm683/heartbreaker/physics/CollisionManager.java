package bham.student.txm683.heartbreaker.physics;

import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashSet;

public class CollisionManager {
    private static final String TAG = "hb::CollisionManager";

    private Grid broadPhaseGrid;

    private LevelState levelState;

    private int collisionCount;

    private HashSet<String> collidedPairNames;

    ArrayList<Pair<Pair<Entity, Entity>, Vector>> collidedPairs;
    ArrayList<ArrayList<Entity>> bins;

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;
        this.collisionCount = 0;
    }

    public void checkCollisions(){
        applySpatialPartitioning();

        applySeparatingAxisTheorem(bins);

        for (Pair<Pair<Entity, Entity>, Vector> collidedPair : collidedPairs){
            collidedPair.first.first.setCollided(true);
            collidedPair.first.second.setCollided(true);

            //Log.d(TAG+collisionCount, "min push vector: " + collidedPair.second.toString());

            Point center = collidedPair.first.first.getShape().getCenter();

            Point newCenter = center.add(collidedPair.second.getRelativeToTailPoint());
            collidedPair.first.first.getShape().setCenter(newCenter);

            //Log.d(TAG+collisionCount, "old center: " + center.toString() + ", new center: " + newCenter.toString());

            this.collisionCount += 1;
        }
    }

    /**
     * Inserts each vertex of every entity into their corresponding position in a grid,
     * returns the cells containing 2 or more entities for more in depth collision checks.
     *
     * @return Returns bins with 2 entities or more in for narrow phase checks
     */
    private void applySpatialPartitioning(){
        //TODO: At the minute, only checking vertices means that edges crossing a different cell reference wont get checked when they should
        Point gridMaximum = new Point(levelState.getMap().getDimensions().first, levelState.getMap().getDimensions().second);

        //int cellSize = 500;
        int cellSize = levelState.getMap().getTileSize() * 2;
        broadPhaseGrid = new Grid(new Point(), gridMaximum, cellSize);

        broadPhaseGrid.addEntityToGrid(levelState.getPlayer());
        //Log.d(TAG, levelState.getPlayer().getName() + " added to grid");

        for (Entity entity : levelState.getStaticEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
            //Log.d(TAG, entity.getName() + " added to grid");
        }

        for (Entity entity : levelState.getEnemyEntities()){
            broadPhaseGrid.addEntityToGrid(entity);
            //Log.d(TAG, entity.getName() + " added to grid");
        }

        //each element will be a bin from a grid reference with more than one entity in
        bins = new ArrayList<>();

        //get the bins that have 2 or more elements in them
        for (Integer column : broadPhaseGrid.getColumnKeySet()){
            for (Integer row : broadPhaseGrid.getRowKeySet(column)){
                ArrayList<Entity> bin = broadPhaseGrid.getBin(column, row);

                if (bin.size() > 1){

                    /*for (Entity entity : bin){
                        entity.setCollided(true);
                    }*/
                    bins.add(bin);
                }
            }
        }
    }

    private void applySeparatingAxisTheorem(ArrayList<ArrayList<Entity>> bins){
        //Log.d(TAG+collisionCount, "STARTING SEP AXIS THM");
        collidedPairs = new ArrayList<>();
        collidedPairNames = new HashSet<>();

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

                        if (collidedPairNames.contains(firstEntity.getName()+secondEntity.getName())){
                            //Log.d(TAG+collisionCount, "already collided: " + firstEntity.getName() + ", " + secondEntity.getName());
                            continue;
                        } else {
                            collidedPairNames.add(firstEntity.getName()+secondEntity.getName());
                            collidedPairNames.add(secondEntity.getName()+firstEntity.getName());
                        }

                        Point[] firstEntityVertices = firstEntity.getShape().getVertices();
                        Point[] secondEntityVertices = secondEntity.getShape().getVertices();

                        ArrayList<Vector> edges = new ArrayList<>(getEdges(firstEntity.getShape().getShapeIdentifier(), firstEntityVertices));
                        edges.addAll(getEdges(firstEntity.getShape().getShapeIdentifier(), secondEntityVertices));

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

                        if (collided){

                            Vector minPushVector = getMinimumPushVector(pushVectors);

                            if (minPushVector.dot(new Vector(firstEntity.getShape().getCenter(), secondEntity.getShape().getCenter())) > 0){
                                minPushVector = minPushVector.sMult(-1f);
                            }

                            //Log.d(TAG+collisionCount, "collided pair added");
                            collidedPairs.add(new Pair<>(new Pair<>(firstEntity, secondEntity), minPushVector));
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

        if (firstEntityMaxLength >= secondEntityMinLength && secondEntityMaxLength >= firstEntityMinLength){
            float pushVectorLength = Math.min((secondEntityMaxLength-firstEntityMinLength), (firstEntityMaxLength-secondEntityMinLength));

            //push a bit more than needed so they dont overlap in future tests to compensate for float precision error
            pushVectorLength += 0.0001f;

            return axis.getUnitVector().sMult(pushVectorLength);
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

        /*switch (shapeIdentifier){
            case RECT:
                //only add first two edges as two remaining edges are parallel.
                try {
                    edges.add(new Vector(vertices[0], vertices[1]));
                    edges.add(new Vector(vertices[1], vertices[2]));
                } catch (IndexOutOfBoundsException e){
                    edges.clear();
                }
                break;
            case ISO_TRIANGLE:
                for (int i = 0; i < vertices.length - 1; i++){
                    edges.add(new Vector(vertices[i], vertices[i+1]));
                }
                edges.add(new Vector(vertices[vertices.length-1], vertices[0]));
                break;
            default:
                break;
        }*/
        for (int i = 0; i < vertices.length - 1; i++){
            edges.add(new Vector(vertices[i], vertices[i+1]));
        }
        edges.add(new Vector(vertices[vertices.length-1], vertices[0]));

        return edges;
    }

    private boolean collisionCheckCircleNotCircle(){

    }

    public Grid getBroadPhaseGrid() {
        return broadPhaseGrid;
    }
}