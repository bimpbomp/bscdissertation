package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.utils.Point;

import java.util.List;

public class PathWrapper {

    private List<Point> path;

    public PathWrapper(List<Point> path){
        this.path = path;
    }

    public List<Point> path(){
        return this.path;
    }
}
