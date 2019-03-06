package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.utils.AStar;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.List;

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
                        Log.d("hb::AI doNothing", "is already running");

                        if (context.containsKeys(TIME_LEFT_IN_IDLE)){
                            int timeLeftInIdle = (int) context.getValue(TIME_LEFT_IN_IDLE) - 1;
                            context.addPair(TIME_LEFT_IN_IDLE, timeLeftInIdle);

                            setMovementToZero(context);

                            if (timeLeftInIdle < 1){
                                Log.d("hb::AI doNothing", "has succeeded");
                                setStatus(SUCCESS);
                            } else {
                                Log.d("hb::AI doNothing", timeLeftInIdle + " time left");
                                setStatus(RUNNING);
                            }
                        }

                    } else {
                        Log.d("hb::AI doNothing", "was not already running");
                        if (context.containsKeys(TIME_PER_IDLE)) {
                            Log.d("hb::AI doNothing", "setting movement to zero");
                            int timePerIdle = (int) context.getValue(TIME_PER_IDLE);
                            context.addPair(TIME_LEFT_IN_IDLE, timePerIdle);

                            setMovementToZero(context);
                        }
                        setStatus(RUNNING);
                    }

                    return getStatus();
                }
                Log.d("hb::AI", "doNothing has failed");
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

    public static BNode plotPath(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, CURRENT_MESH, TARGET, LEVEL_STATE)){
                    AStar a = new AStar((AIEntity) context.getValue(CONTROLLED_ENTITY),
                            ((LevelState) context.getValue(LEVEL_STATE)).getRootMeshPolygons(),
                            ((LevelState) context.getValue(LEVEL_STATE)).getMeshGraph());

                    a.plotPath();

                    return SUCCESS;
                }
                return FAILURE;
            }
        };
    }

    public static BNode followPath(){
        return new BNode() {
            private int pointInPath;

            @Override
            public void construct() {
                super.construct();
                pointInPath = 0;
            }

            @Override
            public void reset(BContext context) {
                super.reset(context);
                pointInPath = 0;
            }

            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, PATH)){

                    AIEntity aiEntity = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    List<Point> path = ((PathWrapper) context.getValue(PATH)).path();

                    if (pointInPath > path.size()-1)
                        return SUCCESS;

                    Point currentPoint = path.get(pointInPath);


                    if (new Vector(aiEntity.getCenter(), currentPoint).getLength() < 100){
                        pointInPath++;

                        if (pointInPath > path.size()-1)
                            return SUCCESS;

                        currentPoint = path.get(pointInPath);
                    }

                    Vector closenessV = new Vector(aiEntity.getCenter(), currentPoint);

                    aiEntity.setRequestedMovementVector(closenessV);
                    return RUNNING;

                }
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

                    Point target = currentMesh.getRandomPointInMesh();

                    Log.d("hb::AI", "target: " + target.toString());
                    context.addPair(TARGET, target);

                    return SUCCESS;
                }
                Log.d("hb::AI", "randomPointInMesh doesnt contains keys");
                return FAILURE;
            }
        };
    }

    public static BNode rotateToTarget(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(TARGET, CONTROLLED_ENTITY)){
                    Log.d("hb::AI", "rotateTo contains keys");
                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point target = (Point) context.getValue(TARGET);

                    Vector rotVector = new Vector(controlled.getCenter(), target).getUnitVector();

                    float angle = Vector.calculateAngleBetweenVectors(controlled.getForwardUnitVector(), rotVector);

                    if (Math.abs(angle) < 0.0872665){
                        Log.d("hb::AI", "no need to rotate... angle:  " + angle);
                        controlled.setRotationVector(Vector.ZERO_VECTOR);
                        return SUCCESS;
                    } else {
                        Log.d("hb::AI", "rotating... angle:  " + angle);
                        controlled.rotateBy(angle);
                        return RUNNING;
                    }
                }
                Log.d("hb::AI", "rotateTo doesnt contains keys");
                return FAILURE;
            }
        };
    }

    public static BNode moveTowardsTarget(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsKeys(CONTROLLED_ENTITY, TARGET)){

                    AIEntity controlled = (AIEntity) context.getValue(CONTROLLED_ENTITY);
                    Point target = (Point) context.getValue(TARGET);

                    Vector movementVector = new Vector(controlled.getCenter(), target);
                    Log.d("hb::AI", "movement vector: " + movementVector.relativeToString() + ", length: " + movementVector.getLength());

                    if (movementVector.getLength() < 100) {
                        controlled.setRequestedMovementVector(Vector.ZERO_VECTOR);
                        return SUCCESS;
                    } else {
                        controlled.setRequestedMovementVector(movementVector.getUnitVector());
                        return RUNNING;
                    }
                }
                Log.d("hb::AI", "moveTowardsTarget doesnt contains keys");
                return FAILURE;
            }
        };
    }
}
