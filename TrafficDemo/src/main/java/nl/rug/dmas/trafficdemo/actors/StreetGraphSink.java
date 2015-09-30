/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import nl.rug.dmas.trafficdemo.Actor;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.Observer;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.streetgraph.Vertex;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

/**
 *
 * @author jelmer
 */
public class StreetGraphSink implements Actor, Observer {
    private final Vertex vertex;
    
    private final Scenario scenario;
    
    public StreetGraphSink(Scenario scenario, Vertex vertex) {
        this.scenario = scenario;
        this.vertex = vertex;
        
        BodyDef def = new BodyDef();
        def.type = BodyType.STATIC;
        def.position = vertex.getLocation();
        
        FixtureDef fovDef = new FixtureDef();
        fovDef.shape = getFOVShape();
        fovDef.isSensor = true;
        fovDef.userData = this;
        
        Body body = scenario.getWorld().createBody(def);
        body.createFixture(fovDef);
    }
    
    private Shape getFOVShape() {
        Shape shape = new CircleShape();
        shape.setRadius(4);
        return shape;
    }
    
    @Override
    public void act() {
        // Nothing for now, we just remove what we see :O
    }

    @Override
    public void addFixtureInSight(Fixture fixture) {
        if (fixture.getUserData() instanceof Car)
            scenario.remove((Car) fixture.getUserData());
    }

    @Override
    public void removeFixtureInSight(Fixture fixture) {
        //
    }
}
