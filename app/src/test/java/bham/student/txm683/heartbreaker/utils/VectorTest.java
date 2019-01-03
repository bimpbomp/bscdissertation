package bham.student.txm683.heartbreaker.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class VectorTest {

    private static final float DELTA = 0.0001f;
    private static final float ROOT2 = (float) Math.sqrt(2f);

    private static final float FX = -1.7445f;
    private static final float FY = 234.7654f;
    private static final float FLENGTH = 234.77188f;

    private static final float GX = 1234.2345f;
    private static final float GY = 234.1234f;
    private static final float GLENGTH = 1256.2438f;

    private Vector a = new Vector();
    private Vector b = new Vector(-1,1);
    private Vector c = new Vector(1,-1);
    private Vector d = new Vector(1,1);
    private Vector e = new Vector(-1,-1);
    private Vector f = new Vector(FX, FY);
    private Vector g = new Vector(GX, GY);

    @Before
    public void setUp() throws Exception {
        a = new Vector();
        b = new Vector(-1,1);
        c = new Vector(1,-1);
        d = new Vector(1,1);
        e = new Vector(-1,-1);
        f = new Vector(FX, FY);
        g = new Vector(GX, GY);
    }

    @Test
    public void getUnitVector() {
        Vector ua = new Vector();
        Vector ub = new Vector(-1/ROOT2,1/ROOT2);
        Vector uc = new Vector(1/ROOT2,-1/ROOT2);
        Vector ud = new Vector(1/ROOT2,1/ROOT2);
        Vector ue = new Vector(-1/ROOT2,-1/ROOT2);
        Vector uf = new Vector(FX/FLENGTH, FY/FLENGTH);
        Vector ug = new Vector(GX/GLENGTH, GY/GLENGTH);

        assertTrue(ua.equals(a.getUnitVector()));
        assertTrue(ub.equals(b.getUnitVector()));
        assertTrue(uc.equals(c.getUnitVector()));
        assertTrue(ud.equals(d.getUnitVector()));
        assertTrue(ue.equals(e.getUnitVector()));
        assertTrue(uf.equals(f.getUnitVector()));
        assertTrue(ug.equals(g.getUnitVector()));


        assertEquals(1f, b.getUnitVector().getLength(), DELTA);
        assertEquals(1f, c.getUnitVector().getLength(), DELTA);
        assertEquals(1f, d.getUnitVector().getLength(), DELTA);
        assertEquals(1f, e.getUnitVector().getLength(), DELTA);
        assertEquals(1f, f.getUnitVector().getLength(), DELTA);
        assertEquals(1f, g.getUnitVector().getLength(), DELTA);
    }

    @Test
    public void getLength() {
        assertEquals(0f,a.getLength(), DELTA);
        assertEquals(ROOT2, b.getLength(), DELTA);
        assertEquals(ROOT2, c.getLength(), DELTA);
        assertEquals(ROOT2, d.getLength(), DELTA);
        assertEquals(ROOT2, e.getLength(), DELTA);
        assertEquals(FLENGTH, f.getLength(), DELTA);
        assertEquals(GLENGTH, g.getLength(), DELTA);
    }

    @Test
    public void staticvAdd() {
        Vector bc = Vector.vAdd(b,c);
        Vector cb = Vector.vAdd(c,b);

        assertEquals(bc.toString(),cb.toString());

        Vector ab = Vector.vAdd(a,b);

        assertEquals(b.toString(), ab.toString());
    }

    @Test
    public void staticsMult() {
        float scalarA = 123.123456789f;
        float scalarB = -234.234f;
        float scalarC = 0f;
        int scalarD = 4;

        assertTrue(a.equals(Vector.sMult(b,scalarC)));

        assertTrue(new Vector(-1*scalarA, scalarA).equals(Vector.sMult(b,scalarA)));

        assertTrue(new Vector(4,4).equals(Vector.sMult(d,scalarD)));

        assertTrue(new Vector(FX*scalarB, FY*scalarB).equals(Vector.sMult(f,scalarB)));
    }

    @Test
    public void equals() {
        assertFalse(a.equals(b));
        assertFalse(b.equals(c));
        assertTrue(a.equals(new Vector()));
        assertFalse(f.equals(g));
    }

    @Test
    public void fromAtoB() {
        assertTrue(Vector.fromAtoB(b,d).equals(new Vector(2f,0)));
        assertTrue(Vector.fromAtoB(f,g).equals(new Vector(g.getX()-f.getX(), g.getY()-f.getY())));

        assertTrue(Vector.fromAtoB(f,g).equals(Vector.sMult(Vector.fromAtoB(g,f), -1)));
    }

    @Test
    public void vAdd() {
        Vector bc = b.vAdd(c);
        Vector cb = c.vAdd(b);

        assertTrue(bc.equals(cb));

        Vector ab = a.vAdd(b);

        assertTrue(b.equals(ab));
    }

    @Test
    public void sMult() {
        float scalarA = 123.123456789f;
        float scalarB = -234.234f;
        float scalarC = 0f;
        int scalarD = 4;

        assertTrue(a.equals(b.sMult(scalarC)));

        assertTrue(new Vector(-1*scalarA, scalarA).equals(b.sMult(scalarA)));

        assertTrue(new Vector(4,4).equals(d.sMult(scalarD)));

        assertTrue(new Vector(FX*scalarB, FY*scalarB).equals(f.sMult(scalarB)));
    }

    @Test
    public void directionTo() {
        assertTrue(b.directionTo(d).equals(new Vector(2f,0)));
        assertTrue(f.directionTo(g).equals(new Vector(g.getX()-f.getX(), g.getY()-f.getY())));
        assertTrue(f.directionTo(g).equals(Vector.sMult(g.directionTo(f), -1)));

        assertTrue(f.directionTo(g).equals(Vector.sMult(Vector.fromAtoB(g,f), -1)));
    }
}