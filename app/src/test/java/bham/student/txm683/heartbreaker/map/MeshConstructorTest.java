package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.framework.map.MeshConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeshConstructorTest {

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

        MeshConstructor meshConstructor = new MeshConstructor();
    }
}