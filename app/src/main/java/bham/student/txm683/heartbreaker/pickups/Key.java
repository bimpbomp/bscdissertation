package bham.student.txm683.heartbreaker.pickups;

import bham.student.txm683.heartbreaker.utils.Point;

public class Key extends Pickup {
    private String unlocks;

    public Key(String name, String nameOfUnlock, Point center, int size) {
        super(name, PickupType.KEY, center, size);
        this.unlocks = nameOfUnlock;
    }

    public String getUnlocks(){
        return unlocks;
    }
}
