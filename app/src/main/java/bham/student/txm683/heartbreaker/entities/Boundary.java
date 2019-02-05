package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;

public class Boundary extends Entity {

    public Boundary(String name, Point spawnCoordinates, int size, int colorValue){
        super(name, spawnCoordinates, ShapeIdentifier.RECT, size, size, colorValue);
    }
}
