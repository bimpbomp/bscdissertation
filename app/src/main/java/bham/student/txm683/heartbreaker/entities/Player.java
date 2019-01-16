package bham.student.txm683.heartbreaker.entities;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.utils.Point;

public class Player extends Entity {

    public Player(String name, Point spawnCoordinates){
        super(name, 150f, new Rectangle(spawnCoordinates, 50, 100, Color.BLACK));
        this.TAG = "hb::Player:"+ name;
    }
}
