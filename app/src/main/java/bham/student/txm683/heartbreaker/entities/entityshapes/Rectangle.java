package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Rectangle extends EntityShape {

    private float angleBetweenUpandPrimaryVectors;

    public Rectangle(Point geometricCenter, float width, float height, int colorValue){
        super(geometricCenter, width, height, colorValue);
        init();
    }

    private void init() {
        this.vertexVectors = new Vector[4];

        float halfWidth = width/2f;
        float halfHeight = height/2f;

        vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY() + halfHeight));
        vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY() + halfHeight));
        vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY() - halfHeight));
        vertexVectors[3] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY() - halfHeight));

        this.relativeUpUnitVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY()+1));

        this.angleBetweenUpandPrimaryVectors = calculateAngleBetweenVectors(vertexVectors[0], this.relativeUpUnitVector);
    }

    @Override
    public void setRelativeUpUnitVector() {

        this.relativeUpUnitVector = rotateVertexVector(vertexVectors[0], (float) Math.cos(angleBetweenUpandPrimaryVectors), (float) Math.sin(angleBetweenUpandPrimaryVectors));
    }

    @Override
    public void setHeight(float newHeight) {
        //TODO
    }

    @Override
    public void setWidth(float newWidth) {
        //TODO
    }
}
