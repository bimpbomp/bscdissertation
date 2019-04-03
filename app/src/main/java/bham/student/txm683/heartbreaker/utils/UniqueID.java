package bham.student.txm683.heartbreaker.utils;

public class UniqueID {
    private int nextID;

    public UniqueID(){
        nextID = 0;
    }

    public UniqueID(int startingID){
        nextID = startingID;
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
     * Returns current counter state without incrementing counter
     * @return Current counter
     */
    public int counter(){
        return nextID;
    }

    /**
     * Resets the counter to 0.
     */
    public void reset(){
        nextID = 0;
    }
}