package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.utils.Tile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MeshConstructorTest {

    @Test
    public void constructMesh() {
        MeshConstructor meshConstructor = new MeshConstructor();

        List<List<Integer>> testTileList = new ArrayList<>();

        /*testTileList.add(Arrays.asList(-1, -1, -1, -1, -1, -1, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, -1, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, -1, -1, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, 0, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, -1, 0, -1, -1, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, 0, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, -1, -1, -1, -1, -1, -1));*/

        testTileList.add(Arrays.asList(-1, -1, -1, -1, -1, -1, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, -1, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, -1, -1, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, 0, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, 0, -1, 0, -1, -1, -1));
        testTileList.add(Arrays.asList(-1, 0, 0, 0, 0, 0, -1));
        testTileList.add(Arrays.asList(-1, -1, -1, -1, -1, -1, -1));

        meshConstructor.constructMesh(testTileList);

        System.out.println("MeshGraph\n");
        System.out.println(meshConstructor.getMeshGraph().toString());

        System.out.println("\n\n");

        for (NSet nSet : meshConstructor.getExistingSets().values()){
            System.out.println("NSet " + nSet.getId() + "\n");

            for (Tile tile : nSet.getContainedTiles()){
                System.out.print(tile + " AND ");
            }
            System.out.println(" END\n");
        }

        assertEquals(2,2);
    }
}