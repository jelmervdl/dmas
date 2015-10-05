/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import nl.rug.dmas.trafficdemo.streetgraph.Vertex;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public abstract class Sign {

    private Vec2 location;

    //The offset of the traffic sign in the visualisation relative to the destination of the edge that the traffic sign is associated with.
    public static Vec2 offset = new Vec2(1f, 1f);

    public Sign() {
    }

    public void setLocation(Vec2 location) {
        this.location = location;
    }
    
    public static Vec2 computeLocation(Vertex originVertex, Vertex destinationVertex){
        Vec2 origin = originVertex.getLocation();
        Vec2 destination = destinationVertex.getLocation();
        float tx = (destination.x - offset.x - origin.x) / (destination.x - origin.x);
        float ty = (destination.y - offset.y - origin.y) / (destination.y - origin.y);
        Vec2 directionVector = destination.sub(origin);
        return new Vec2(
                origin.x + tx * directionVector.x,
                origin.y + ty * directionVector.y
        );
    }
            
    
    

}
