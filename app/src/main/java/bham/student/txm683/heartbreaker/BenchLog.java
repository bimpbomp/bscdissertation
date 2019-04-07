package bham.student.txm683.heartbreaker;

import android.util.Log;
import bham.student.txm683.framework.utils.GameTickTimer;

import java.util.ArrayList;
import java.util.List;

public class BenchLog {

    private List<Long> fgTime, fgchecks, rgTime, rgChecks, rgInsertions, itemsOnScreen, renderTimes;
    private MainActivity context;

    private boolean active;
    private GameTickTimer timer;

    public BenchLog(MainActivity context){
        fgTime = new ArrayList<>();
        rgTime = new ArrayList<>();
        rgChecks = new ArrayList<>();
        rgInsertions = new ArrayList<>();
        renderTimes = new ArrayList<>();
        fgchecks = new ArrayList<>();

        itemsOnScreen = new ArrayList<>();

        this.context = context;

        this.active = false;
        //5 seconds
        this.timer = new GameTickTimer(5000);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;

        if (active){

            Log.d("BENCHMARKING", "starting benchmark");
            timer.start();

            fgTime.clear();
            rgTime.clear();
            rgChecks.clear();
            rgInsertions.clear();
            renderTimes.clear();
            itemsOnScreen.clear();
        }
    }

    public void addSnapshot(long fgtime, long rgtime, long rgCheck, long rgInsertion, long fgcheck){
        if (active) {
            fgTime.add(fgtime);
            rgTime.add(rgtime);
            rgChecks.add(rgCheck);
            rgInsertions.add(rgInsertion);
            fgchecks.add(fgcheck);

            Log.d("BENCHMARK", "snapshot added");
            tick();
        }
    }

    public void tick(){
        if (timer.tick() > 0){
            Log.d("BENCHMARKING", "benchmark ending...");
            active = false;
            timer.stop();

            //printAverages();
            Log.d("BENCHMARKING", "average items on screen: " + calcAverage(itemsOnScreen) + ", average render tick: " + calcAverage(renderTimes));
        }
    }

    public void addRender(long render){
        renderTimes.add(render);
    }

    public void addItemsOnScreenCount(int num){
        itemsOnScreen.add((long) num);
    }

    public void printAverages(){

        Log.d("BENCHMARKING", "Averages: " + "FGTime: " + calcAverage(fgTime) + ", RGTime: " + calcAverage(rgTime) +
                ", FGChecks: " + calcAverage(fgchecks) + ", RGChecks: " + calcAverage(rgChecks));
    }

    private static long calcAverage(List<Long> list){
        if (list.size() == 0)
            return 0;

        long sum = list.stream().mapToLong(Long::longValue).sum();
        return sum/list.size();
    }
}