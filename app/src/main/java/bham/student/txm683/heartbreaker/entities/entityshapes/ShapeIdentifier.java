package bham.student.txm683.heartbreaker.entities.entityshapes;

public enum ShapeIdentifier {

    ISO_TRIANGLE(0),
    EQU_TRIANGLE(1),
    SQUARE(2),
    RECT(3);
    

    private final int id;

    ShapeIdentifier(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
