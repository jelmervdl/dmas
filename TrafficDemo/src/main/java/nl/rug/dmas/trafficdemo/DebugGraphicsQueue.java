/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class DebugGraphicsQueue {
    static public interface Renderer {
        public void drawPositionVelocity(Vec2 position, Vec2 velocity);
        
        public void drawPosition(Vec2 position);
    }
    
    static public interface DrawCall {
        public void draw(Renderer renderer); 
    }
    
    static public class DrawPositionVelocityCall implements DrawCall {
        final private Vec2 position, velocity;

        public DrawPositionVelocityCall(Vec2 position, Vec2 velocity) {
            this.position = position;
            this.velocity = velocity;
        }
        
        @Override
        public void draw(Renderer renderer) {
            renderer.drawPositionVelocity(position, velocity);
        }
    }
    
    static public class DrawPositionCall implements DrawCall {
        final private Vec2 position;

        public DrawPositionCall(Vec2 position) {
            this.position = position;
        }
        
        @Override
        public void draw(Renderer renderer) {
            renderer.drawPosition(position);
        }
    }
    
    final private List<DrawCall> calls = new CopyOnWriteArrayList<>();
    
    public void clear() {
        calls.clear();
    }
    
    public void draw(Vec2 position, Vec2 velocity) {
        calls.add(new DrawPositionVelocityCall(position, velocity));
    }
    
    public void draw(Vec2 position) {
        calls.add(new DrawPositionCall(position));
    }
    
    public void renderTo(Renderer renderer) {
        for (DrawCall call : calls)
            call.draw(renderer);
    }
}
