package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Rectangle extends EntityShape {

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

        setRelativeUpUnitVector();
    }

    @Override
    public void setRelativeUpUnitVector() {
        float cosandsin45 = 0.7071f;

        this.relativeUpUnitVector = rotateVertexVector(vertexVectors[0], cosandsin45, cosandsin45);
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
