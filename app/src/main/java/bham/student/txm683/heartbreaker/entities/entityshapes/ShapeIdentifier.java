package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public enum ShapeIdentifier {

    INVALID(-1),
    ISO_TRIANGLE(0),
    RECT(1),
    CIRCLE (2);
    

    private final int id;

    ShapeIdentifier(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, ShapeIdentifier> intToTypeMap = new HashMap<>();
    static {
        for (ShapeIdentifier type : ShapeIdentifier.values()) {
            intToTypeMap.put(type.getId(), type);
        }
    }

    public static ShapeIdentifier fromInt(int i) {
        ShapeIdentifier type = intToTypeMap.get(i);
        if (type == null)
            return ShapeIdentifier.INVALID;
        return type;
    }
}
