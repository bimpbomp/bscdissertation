package bham.student.txm683.heartbreaker.ai.behaviours;

public abstract class BNode {
    private Status status;

    public BNode(){
        status = Status.FAILURE;
    }

    abstract void init();

    abstract Status process();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
