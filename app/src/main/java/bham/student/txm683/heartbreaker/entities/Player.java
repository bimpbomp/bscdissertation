package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTrapezium;
import bham.student.txm683.heartbreaker.utils.Point;
import org.json.JSONException;

import java.text.ParseException;

public class Player extends MoveableEntity {

    public Player(String name, Point spawnCoordinates, int size, float maxSpeed, int color){
        super(name, new IsoscelesTrapezium(spawnCoordinates, (size/2f), (size/2f), (size/2f), color), maxSpeed, 25);
    }

    public Player(String stateString) throws ParseException, JSONException {
        super(stateString);
    }
}