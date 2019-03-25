package bham.student.txm683.heartbreaker.map;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeshConstructorV2Test {

    @Test
    public void constructMesh() {
        List<List<Integer>> testTileList = new ArrayList<>();

        testTileList.add(Arrays.asList(-1, -1, -1, -1, -1, -1, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, -1, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, -1, -1, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, 0, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, -1, 0, -1, -1, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, 0, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, -1, -1, -1, -1, -1, -1));

        MeshConstructorV2 meshConstructorV2 = new MeshConstructorV2();
    }
}