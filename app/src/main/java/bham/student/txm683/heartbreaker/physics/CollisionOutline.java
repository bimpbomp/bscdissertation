package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.utils.Vector;

public class CollisionOutline {
    private Vector[] vertexVectors;

    public CollisionOutline(Vector[] vertexVectors){
        this.vertexVectors = vertexVectors;
    }

    /*public Point[] getCollisionVertices(){
        return Vector.getVertices(vertexVectors);
    }*/

    public Vector getVertexVector(int idx){
        if (idx >= 0 && idx < vertexVectors.length)
            return vertexVectors[idx];
        return new Vector();
    }

    public void setVertexVector(Vector vector, int idx){
        if (idx >= 0 && idx < vertexVectors.length)
            this.vertexVectors[idx] = vector;
    }

    public void setVertexVectors(Vector[] vertexVectors) {
        this.vertexVectors = vertexVectors;
    }
}
