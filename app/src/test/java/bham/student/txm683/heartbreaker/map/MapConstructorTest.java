package bham.student.txm683.heartbreaker.map;

import org.junit.Test;

public class MapConstructorTest {

    @Test
    public void generateWallsV2() {
        MapConstructor constructor = new MapConstructor();

        constructor.generateWallsV2(MapConstructor.tileList);
    }
}