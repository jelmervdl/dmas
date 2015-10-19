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
    @org.junit.Ignore
    public void testIntersect() {
        System.out.println("intersect");
        Vec2 as = new Vec2(0, 0);
        Vec2 ad = new Vec2(1, 0);
        Vec2 bs = new Vec2(0, 2);
        Vec2 bd = new Vec2(2, -1);
        VecUtils.Intersection result = VecUtils.intersect(as, ad, bs, bd);
        
        // Assume there is an intersection
        assertNotNull(result);
        
        // Assume the intersection is at [0,4]
        assertEquals(4.0f, result.position.x, 0.0001f);
        assertEquals(0.0f, result.position.y, 0.0001f);
        
        // Assume that the [0,0; 1,0] arrives there later than the [2,-1] one
        assertTrue(result.v > result.u);
    }

    /**
     * Test of rotate method, of class VecUtils.
     */
    @org.junit.Test
    public void testRotate() {
        System.out.println("rotate");
        assertEquals(new Vec2(0, 1), VecUtils.rotate(new Vec2(1,0), MathUtils.HALF_PI));
        
        Vec2 expect = new Vec2(0, -1);
        Vec2 result = VecUtils.rotate(new Vec2(1,0), 3 * MathUtils.HALF_PI);
        assertEquals(expect.x, result.x, 0.0001f);
        assertEquals(expect.y, result.y, 0.0001f);
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
        
        vector = new Vec2(1, 0);
        assertEquals(0.0f, VecUtils.getAngle(vector), 0.0001f);
        
        vector = new Vec2(0, 1);
        assertEquals(MathUtils.HALF_PI, VecUtils.getAngle(vector), 0.0001f);
    }
    
}
