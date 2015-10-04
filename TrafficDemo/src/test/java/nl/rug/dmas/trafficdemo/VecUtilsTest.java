/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import static org.junit.Assert.*;

/**
 *
 * @author jelmer
 */
public class VecUtilsTest {
    
    /**
     * Test of intersect method, of class VecUtils.
     */
    @org.junit.Test
    public void testIntersect() {
        System.out.println("intersect");
        Vec2 as = null;
        Vec2 ad = null;
        Vec2 bs = null;
        Vec2 bd = null;
        VecUtils.Intersection expResult = null;
        VecUtils.Intersection result = VecUtils.intersect(as, ad, bs, bd);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rotate method, of class VecUtils.
     */
    @org.junit.Test
    public void testRotate() {
        System.out.println("rotate");
        assertEquals(new Vec2(0, 1), VecUtils.rotate(new Vec2(1,0), MathUtils.HALF_PI));
        
        assertEquals(new Vec2(0, -1), VecUtils.rotate(new Vec2(1,0), 3 * MathUtils.HALF_PI));
    }

    /**
     * Test of getAngle method, of class VecUtils.
     */
    @org.junit.Test
    public void testGetAngle() {
        System.out.println("getAngle");
        Vec2 vector = new Vec2(1, 1);
        float expResult = MathUtils.QUARTER_PI;
        float result = VecUtils.getAngle(vector);
        assertEquals(expResult, result, 0.01);
    }
    
}
