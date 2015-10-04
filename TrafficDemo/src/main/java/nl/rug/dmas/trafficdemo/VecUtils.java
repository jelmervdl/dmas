/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class VecUtils {
    static public class Intersection
    {
        final public float u, v;
        
        final public Vec2 position;
        
        public Intersection(float u, float v, Vec2 position) {
            this.u = u;
            this.v = v;
            this.position = position;
        }
    }
    
    /**
     * Calculate whether two moving objects are going to intersect, and when.
     * @param as Position of a
     * @param ad Velocity of a
     * @param bs Position of b
     * @param bd Velocity of b
     * @return the moment and position of intersection, or null if there is no intersection
     */
    static public Intersection intersect(Vec2 as, Vec2 ad, Vec2 bs, Vec2 bd) {
        // Let's try to solve:
        // p = as + ad * u
        // p = bs + bd * v
        
        float dx = bs.x - as.x;
        float dy = bs.y - as.y;
        float det = bd.x * ad.y - bd.y * ad.x;
        float u = (dy * bd.x - dx * bd.y) / det;
        float v = (dy * ad.x - dx * ad.y) / det;
        
        if (u > 0 && v > 0)
            return new Intersection(u, v, as.mul(u));
        else
            return null;
    }
    
    /**
     * Rotate a vector by angle
     * @param vector
     * @param angle in radians
     * @return rotated vector
     */
    static public Vec2 rotate(Vec2 vector, float angle) {
        return new Vec2(
            vector.x * MathUtils.cos(angle) - vector.y * MathUtils.sin(angle),
            vector.x * MathUtils.sin(angle) + vector.y * MathUtils.cos(angle)
        );
    }
    
    /**
     * Get the angle of a vector
     * @param vector
     * @return rotation in radians
     */
    static public float getAngle(Vec2 vector) {
        Vec2 normalized = new Vec2(vector);
        normalized.normalize();
        return MathUtils.atan2(normalized.y, normalized.x);
    }
}
