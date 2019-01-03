package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.utils.Vector;

public class Player extends Entity {

    public Player(String name, Vector spawnCoordinates){
        super(name, spawnCoordinates);
        this.TAG = "hb::Player:"+ name;
    }
}
