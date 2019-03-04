package bham.student.txm683.heartbreaker.ai.behaviours;

public abstract class BNode {
    private Status status;

    public BNode(){
        status = Status.READY;
    }

    public void reset(BContext context){
        this.status = Status.READY;
    }

    public abstract Status process(BContext context);

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
