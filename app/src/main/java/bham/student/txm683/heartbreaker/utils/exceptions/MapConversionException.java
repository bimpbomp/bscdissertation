package bham.student.txm683.heartbreaker.utils.exceptions;

public class MapConversionException extends Exception {

    private MCEReason r;

    public MapConversionException(MCEReason r, Throwable err){
        super(r.name(), err);
        this.r = r;
    }

    public MapConversionException(MCEReason r){
        super("MapConversionException thrown with reason: " + r.name());
        this.r = r;
    }

    public MCEReason getR() {
        return r;
    }
}

