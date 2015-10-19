/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
abstract public class ShapeUtil {
    public static PolygonShape createArc(float radius, float start, float stop, Vec2 origin)
    {
        float step = (stop - start) / 7;
        
        Vec2[] vertices = new Vec2[8];
        vertices[0] = new Vec2(origin);
        for (int i = 0; i < 7; ++i) {
            float angle = start + i * step;
            vertices[i + 1] = new Vec2(origin.x + radius * MathUtils.cos(angle), origin.y + radius * -MathUtils.sin(angle));
        }
        
        PolygonShape shape = new PolygonShape();
        shape.set(vertices, vertices.length);
        return shape;
    }
    
    public static PolygonShape createArc(float radius, float start, float stop) {
        return createArc(radius, start, stop, new Vec2(0, 0));
    }
}
