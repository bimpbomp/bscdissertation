package bham.student.txm683.framework.utils;

import android.util.Log;

public class BenchMarker {
    private long startTime;

    public BenchMarker(){
        startTime = 0;
    }

    public void begin(){
        startTime = System.currentTimeMillis();
    }

    public void output(String label){
        Log.d("BENCHMARK", label + ": " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public long getTime(){
        return (System.currentTimeMillis() - startTime);
    }
}
