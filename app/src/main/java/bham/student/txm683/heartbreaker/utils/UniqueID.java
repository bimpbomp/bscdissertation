package bham.student.txm683.heartbreaker.utils;

public class UniqueID {
    private int nextID;

    /**
     * New objects should default to this value until assigned a unique value
     */
    public static final int UNASSIGNED = -1;

    public UniqueID(){
        nextID = 0;
    }

    /**
     * Returns an int for use as a unique identifier for an object, auto-increments internal counter to ensure
     * returned values are unique.
     * @return next id
     */
    public int id(){
        return nextID++;
    }

    /**
     * Resets the counter to 0.
     */
    public void reset(){
        nextID = 0;
    }
}