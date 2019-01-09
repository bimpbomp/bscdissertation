package bham.student.txm683.heartbreaker.entities;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Player extends Entity {

    public Player(String name, Vector spawnCoordinates){
        super(name, spawnCoordinates, Color.BLACK);
        this.TAG = "hb::Player:"+ name;
    }
}
