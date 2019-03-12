package bham.student.txm683.heartbreaker.pickups;

import bham.student.txm683.heartbreaker.utils.Point;

public class Key extends Pickup {
    private String unlocks;

    public Key(int name, String unlocks, Point center, int size) {
        super("Key"+name, PickupType.KEY, center, size);
        this.unlocks = unlocks;
    }

    public Key(int name, String unlocks, Point center){
        this(name, unlocks, center, 50);
    }

    public String getUnlocks(){
        return unlocks;
    }
}
