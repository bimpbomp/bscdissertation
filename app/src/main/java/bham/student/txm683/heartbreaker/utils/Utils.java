package bham.student.txm683.heartbreaker.utils;

public class Utils {

    public Utils(){

    }

    public static void sleep(int sleepTimeMillis){
        if (sleepTimeMillis < 10000){
            try {
                Thread.sleep(sleepTimeMillis);
            } catch (InterruptedException e){
                //do nothing
            }
        }
    }
}
