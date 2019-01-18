package bham.student.txm683.heartbreaker.entities;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.utils.Point;
import org.json.JSONException;

import java.text.ParseException;

public class Player extends Entity {

    public Player(String name, Point spawnCoordinates){
        super(name, 250f, new Rectangle(spawnCoordinates, 50, 50, Color.BLACK));
        this.TAG = "hb::Player:"+ name;
    }

    public Player(String stateString) throws ParseException, JSONException {
        super(stateString);
        //TODO add name to tag as in above constructor
    }
}
