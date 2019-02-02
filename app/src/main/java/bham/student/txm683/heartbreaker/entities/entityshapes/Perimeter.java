package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Un-rotatable shape for defining a region as a room
 */
public class Perimeter extends Polygon {

    /**
     *
     * @param vertices Vertices of perimeter defined in a clockwise manner, starting at the top left vertex
     */
    public Perimeter(Point[] vertices){
        super(vertices[0], 0, 0, Color.WHITE, ShapeIdentifier.INVALID);

        this.vertexVectors = new Vector[vertices.length];

        for (int i = 0; i < vertices.length; i++){
            this.vertexVectors[i] = new Vector(geometricCenter, vertices[i]);
        }
    }

    /**
     * This function does nothing, and is overridden to prevent the shape being rotated
     * @param movementVector Irrelevant
     */
    @Override
    public void rotateShape(Vector movementVector) {

    }

    /**
     * Set to the zero vector as it is not needed (no rotations)
     */
    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = new Vector();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        return null;
    }
}
