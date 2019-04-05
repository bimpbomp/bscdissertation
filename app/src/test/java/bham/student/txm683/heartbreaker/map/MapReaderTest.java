package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.framework.map.MapConstructor;
import bham.student.txm683.heartbreaker.MainActivity;
import org.junit.Test;

import java.util.ArrayList;

public class MapReaderTest {

    @Test
    public void loadMap() {

        MainActivity m = new MainActivity();
        Map map = new Map("meshdemo2", "1", 200);
        MapConstructor mc = new MapConstructor(m, map);

        mc.loadMap(new ArrayList<>());

        System.out.println("nodes: " + map.getMeshGraph().getNodes().size());
    }
}