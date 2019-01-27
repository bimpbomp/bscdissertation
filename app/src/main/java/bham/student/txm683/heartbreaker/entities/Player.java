package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.utils.Point;
import org.json.JSONException;

import java.text.ParseException;

public class Player extends MoveableEntity {

    public Player(String name, Point spawnCoordinates, int size, float maxSpeed, int color){
        super(name, new Rectangle(spawnCoordinates, size/2f, size/2f, color), maxSpeed);
        //super(name, new Circle(spawnCoordinates, size/2f, Color.BLACK), maxSpeed);
    }

    public Player(String stateString) throws ParseException, JSONException {
        super(stateString);
    }
}