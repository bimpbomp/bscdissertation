package bham.student.txm683.framework.physics;

import android.graphics.Color;
import android.util.Pair;
import bham.student.txm683.framework.ai.IAIEntity;
import bham.student.txm683.framework.entities.Door;
import bham.student.txm683.framework.entities.MoveableEntity;
import bham.student.txm683.framework.entities.entityshapes.Circle;
import bham.student.txm683.framework.entities.entityshapes.Rectangle;
import bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.UniqueID;
import bham.student.txm683.framework.utils.Vector;

import java.util.ArrayList;
import java.util.List;

import static bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier.RECTANGLE;

public class CollisionTools {
    private static final String TAG = "CollisionTools";

    //used to combat floating point inaccuracies
    private static final float PUSH_VECTOR_ERROR = 0.001f;

    private CollisionTools(){

    }

    public static List<SpatialBin> initBins(int numHCells, int numVCells, int cellWidth, int cellHeight){
        UniqueID uniqueID = new UniqueID();

        List<SpatialBin> spatialBins = new ArrayList<>();

        //create the spatial bins
        for (int i = 1; i <= numHCells; i++){
            int l = (i-1) * cellWidth;
            int r = i * cellWidth;

            for (int j = 1; j <= numVCells; j++){
                int t = (j-1) * cellHeight;
                int b = j * cellHeight;

                spatialBins.add(new SpatialBin(uniqueID.id(), new BoundingBox(l,t,r,b)));
            }
        }

        return spatialBins;
    }

    public static boolean addTempToBins(Collidable collidable, List<SpatialBin> spatialBins){
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

    public static List<Boolean> addTempsToBins(List<Collidable> collidables, List<SpatialBin> spatialBins){

        List<Boolean> successes = new ArrayList<>();

        for (Collidable collidable : collidables){
            successes.add(addTempToBins(collidable, spatialBins));
        }
        return successes;
    }

    public static void fillBinsWithPermanents(List<Collidable> collidables, List<SpatialBin> bins){
        //add each static to the permanent list in the correct spatial bin
        for (Collidable collidable : collidables){

            for (SpatialBin bin : bins){

                if (bin.getBoundingBox().intersecting(collidable.getBoundingBox())){

                    if (collidable instanceof Door){
                        //if the collidable is a door, add it's interaction field to the bins
                        bin.addPermanent(((Door) collidable).getPrimaryField());
                    }

                    //if the collidable intersects this bin's bounding box, add it to the bin's permanent list
                    bin.addPermanent(collidable);
                }
            }
        }
    }

    public static void resolveSolidsCollision(Collidable firstCollidable, Collidable secondCollidable, Vector pushVector, List<Collidable> bin){
        Point newCenter;

        if (firstCollidable.canMove() && secondCollidable.canMove()) {
            //both entities can move

            //amount added to the entity's center to allow reverting to previous state
            Point firstAmountMoved;
            Point secondAmountMoved;

            int firstMass = ((MoveableEntity) firstCollidable).getMass();
            int secondMass = ((MoveableEntity) secondCollidable).getMass();

            if (firstMass == secondMass){
                //if they have the same mass, move both equally
                firstAmountMoved = pushVector.sMult(0.5f).getRelativeToTailPoint();
                secondAmountMoved = pushVector.sMult(-0.5f).getRelativeToTailPoint();
            } else if (firstMass > secondMass){
                //if first has greater mass, only move second
                firstAmountMoved = new Point();
                //since the push vector always aims to push the first entity, it needs to be flipped
                secondAmountMoved = pushVector.sMult(-1).getRelativeToTailPoint();
            } else {
                //second has greater mass, only move first
                firstAmountMoved = pushVector.getRelativeToTailPoint();
                secondAmountMoved = new Point();
            }


            //update object positions with calculated movement amounts
            newCenter = firstCollidable.getCenter().add(firstAmountMoved);
            firstCollidable.setCenter(newCenter);

            newCenter = secondCollidable.getCenter().add(secondAmountMoved);
            secondCollidable.setCenter(newCenter);

        } else {
            //only one entity can move

            MoveableEntity moveableCollidable;

            //find out which entity is able to move
            if (firstCollidable.canMove()) {
                moveableCollidable = (MoveableEntity) firstCollidable;

            } else {
                moveableCollidable = (MoveableEntity) secondCollidable;

                //since the push vector always aims to push the first entity, it needs to be flipped
                pushVector = pushVector.sMult(-1f);
            }

            //move the entity
            Point amountToMove = pushVector.getRelativeToTailPoint();
            moveEntityCenter(moveableCollidable, amountToMove);

            //the bigger the angle at which the moveable entity collides with the immovable,
            //the less force that will be exerted (glancing blow)
            float angle = Vector.calculateAngleBetweenVectors(moveableCollidable.getVelocity().sMult(-1), pushVector);
            float prop = 1 - (angle / (float)Math.PI);

            //add a force to the entity to force them away from the wall
            moveableCollidable.addForce(pushVector.setLength(100).sMult(prop));
        }
    }

    public static float getWiggleRoom(MoveableEntity player, IAIEntity aiEntity){
        //this algorithm is used in the event that the ai is aiming at the player.
        //it calculates the maximum angle away from the player's center that the ai can aim but still
        //potentially land a hit


        Point[] playerVertices = player.getCollisionVertices();

        //calculate the normal to the vector between the ai and the player
        Vector aToP = new Vector(aiEntity.getCenter(), player.getCenter());
        Vector normal = aToP.rotateAntiClockwise90().getUnitVector();

        //find the max and min vertices of the player on this normal axis
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

        //   ____
        //  |____|  player
        //  ^    ^
        // min   max
        //
        //  ----->  normal vector
        //
        //   _
        //  |_|     ai


        Vector aToMin = new Vector (aiEntity.getCenter(), minPoint);
        Vector aToMax = new Vector (aiEntity.getCenter(), maxPoint);

        float minAngle = Math.abs(Vector.calculateAngleBetweenVectors(aToP, aToMin));
        float maxAngle = Math.abs(Vector.calculateAngleBetweenVectors(aToP, aToMax));

        return Math.min(minAngle, maxAngle);
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

    public static Vector collisionCheckCircleAndPolygon(Circle circle, Collidable polygon){
        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon.getShapeIdentifier(), polygon.getCollisionVertices()));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        Vector pushVector;
        ArrayList<Vector> pushVectors = new ArrayList<>();
        for (Vector axis : orthogonalAxes){

            //evaluate if the axis is a separating vector, projecting the circle onto it to gain
            //it's max and min points
            pushVector = isSeparatingAxis(axis, polygon.getCollisionVertices(),
                    getCircleVerticesForAxis(axis, circle.getCenter(), circle.getRadius()));

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                return Vector.ZERO_VECTOR;
            }
            pushVectors.add(pushVector);
        }

        return getMinimumPushVector(pushVectors, circle.getCenter(), polygon.getCenter());
    }

    /**
     *
     * Used to steer around any obstacles in the way of the entity.
     * Note: this is intended to only be used to steer away from entities
     * that aren't in the navmesh.
     *
     * @param entity the entity to steer
     * @param target point that the entity is heading towards
     * @param avoidables list of obstacles not in the navmesh
     * @return The calculated steering vector
     */
    public static Vector getPathAroundObstacle(IAIEntity entity, Point target, List<Collidable> avoidables){

        Collidable closestCollidable = null;
        int smallestDistance = Integer.MAX_VALUE;

        Vector fUnit = entity.getForwardUnitVector();

        float height = 200f;

        //form a rectangle extending out in front of the ai to use in collision testing
        Vector v = new Vector(entity.getCenter(), target);
        Point center = v.setLength(height/2).getHead();
        Vector steeringAxis = v.rotateAntiClockwise90();

        Rectangle rect = new Rectangle(center, entity.getWidth()*1.5f, height, Color.GRAY);
        Point[] rectVertices = rect.getVertices();

        Vector pV;

        for (Collidable collidable : avoidables){
            //iterate through possible obstacles

            if (collidable.getName().equals(entity.getName()))
                continue;

            if (collidable instanceof MoveableEntity){
                //if the entity is not in the navmesh


                int distance = euclideanHeuristic(entity.getCenter(), collidable.getCenter());

                //check if it is in the way of the ai's "sight" rectangle
                pV = collisionCheckTwoPolygons(collidable.getCollisionVertices(), collidable.getCenter(), collidable.getShapeIdentifier(),
                        rectVertices, rect.getCenter(), rect.getShapeIdentifier());

                if (!pV.equals(Vector.ZERO_VECTOR) && distance < smallestDistance){
                    //if the collidable is closer than the stored collidable, override it
                    smallestDistance = distance;
                    closestCollidable = collidable;
                }
            }
        }

        if (closestCollidable == null)
            return Vector.ZERO_VECTOR;

        //ai are told to always turn left when potentially colliding with other ai
        //this is to avoid them turning into each other
        if (closestCollidable instanceof IAIEntity){
            return fUnit.rotateClockwise90();
        }

        //determine the direction to turn by using the vector from the ai to the collidable and the forward vector
        Vector ray = new Vector(entity.getCenter(), closestCollidable.getCenter()).getUnitVector();

        float det = fUnit.det(ray);

        //if the ai is aiming towards the left of the collidable, go that way
        //as there is less distance to turn
        if (det < 0) {
            steeringAxis = steeringAxis.sMult(-1f);
        }

        return steeringAxis;
    }

    public static int getClosestPointOnPathIdx(Point position, List<Point> path){
        //iterates through a given path and returns the index of the point closest to the given position

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

    public static int euclideanHeuristic(Point point, Point point1){
        return (int) new Vector(point, point1).getLength();
    }

    public static boolean collisionCheckRay(Collidable collidable, Vector ray){
        Point[] rayVertices = new Point[]{ray.getTail(), ray.getHead()};

        //use the axes of the collidable for SAT tests.
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

    public static List<Vector> applySAT(Vector[] axes, Point[] firstEntityVertices, Point[] secondEntityVertices){
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

    public static Vector reflectVectorAcrossAxis(Vector initialVector, Vector reflectionAxis){
        reflectionAxis = reflectionAxis.getUnitVector();
        float dot = reflectionAxis.dot(initialVector);

        return initialVector.vSub(reflectionAxis.sMult(dot*2));
    }

    public static Point[] getCircleVerticesForAxis(Vector axis, Point center, float radius){
        //projects the circle with given center and radius onto the given axis to obtain
        //the maximum and minimum points

        //create a vector with length equal to the radius
        Vector radiusVector = axis.sMult(radius);

        //calculate max point on this axis by adding a radius onto the center in the direction of the axis
        //calculate min by subtracting a radius from the center in the direction of the axis
        return new Point[]{center.add(radiusVector.getRelativeToTailPoint()),
                center.add(radiusVector.sMult(-1f).getRelativeToTailPoint())};
    }

    public static void moveEntityCenter(Collidable entity, Point amountToMove){
        Point newCenter = entity.getCenter().add(amountToMove);
        entity.setCenter(newCenter);
    }

    public static Vector getMinimumPushVector(List<Vector> pushVectors, Point center1, Point center2){


        Vector minPushVector = Vector.ZERO_VECTOR;

        //iterate through the pushvectors, keeping the smallest one as you progress
        if (pushVectors.size() > 0) {
            minPushVector = pushVectors.get(0);

            for (Vector pushVector : pushVectors) {
                minPushVector = minPushVector.getLength() < pushVector.getLength() ? minPushVector : pushVector;
            }
        }

        //if the push vector is pointing towards the second entity, invert it
        //this is so the direction of the push vector is consistently pointing towards the first entity
        if (minPushVector.dot(new Vector(center1, center2)) > 0) {
            minPushVector = minPushVector.sMult(-1f);
        }
        return minPushVector;
    }

    //converts the given vertices into vectors from the origin to the vertex
    public static Vector[] convertToVectorsFromOrigin(Point[] vertices){
        Vector[] verticesVectors = new Vector[vertices.length];
        for (int i = 0; i < vertices.length; i++){
            verticesVectors[i] = new Vector(vertices[i]);
        }
        return verticesVectors;
    }

    //checks if the max and min points on the given direction axis overlap, returns the overlap
    public static Vector isSeparatingAxis(Vector axis, Point[] firstEntityVertices, Point[] secondEntityVertices){
        Pair<Float, Float> minMaxResult = projectOntoAxis(axis, firstEntityVertices);

        float firstEntityMinLength = minMaxResult.first;
        float firstEntityMaxLength = minMaxResult.second;

        minMaxResult = projectOntoAxis(axis, secondEntityVertices);
        float secondEntityMinLength = minMaxResult.first;
        float secondEntityMaxLength = minMaxResult.second;

        if (firstEntityMaxLength >= secondEntityMinLength && secondEntityMaxLength >= firstEntityMinLength) {
            float pushVectorLength = Math.min((secondEntityMaxLength - firstEntityMinLength),
                    (firstEntityMaxLength - secondEntityMinLength));

            //push a bit more than needed so they dont overlap in future tests
            //to compensate for float precision error
            pushVectorLength += PUSH_VECTOR_ERROR;

            return axis.getUnitVector().sMult(pushVectorLength);
        }
        return Vector.ZERO_VECTOR;
    }

    public static Pair<Float, Float> projectOntoAxis(Vector axis, Point... vertices){
        float minLength = Float.POSITIVE_INFINITY;
        float maxLength = Float.NEGATIVE_INFINITY;

        float projection;

        for (Vector vertexVector : convertToVectorsFromOrigin(vertices)){
            //iterate through the vertices, projecting them onto the axis,
            //keeping the maximum and minimum projections as you go

            projection = vertexVector.dot(axis);

            minLength = Math.min(minLength, projection);
            maxLength = Math.max(maxLength, projection);
        }

        return new Pair<>(minLength, maxLength);
    }

    //Returns the unit normals for the given edges.
    //Rotated anticlockwise, so assumes the edges given cycle clockwise around a shape
    public static Vector[] getEdgeNormals(ArrayList<Vector> edges){
        Vector[] orthogonals = new Vector[edges.size()];

        for (int i = 0; i < edges.size(); i++){
            orthogonals[i] = getEdgeNormal(edges.get(i));
        }
        return orthogonals;
    }

    public static Vector getEdgeNormal(Vector edge){
        return edge.rotateAntiClockwise90().getUnitVector();
    }

    //joins the vertices together to form edge vectors.
    //joins the last and the first vertex to form a closed shape
    //if the shape is a rectangle, only the orthogonal edges are returned
    public static ArrayList<Vector> getEdges(ShapeIdentifier shapeIdentifier, Point[] vertices){
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