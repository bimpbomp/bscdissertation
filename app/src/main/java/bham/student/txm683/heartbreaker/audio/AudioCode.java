package bham.student.txm683.heartbreaker.audio;

public enum AudioCode {

    COLLISION (0);

    private int code;

    AudioCode(int code){
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }
}
