/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.HashSet;
import java.util.Set;
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
public class Crossing implements Actor, Observer {
    private final Vertex vertex;
    
    private final Scenario scenario;
    
    private final Set<Car> carsInSight = new HashSet<>();
    
    private final float stuckTime;
    
    private float lastUpdate;
    
    public Crossing(Scenario scenario, Vertex vertex, float radius, float stuckTime) {
        this.scenario = scenario;
        this.vertex = vertex;
        this.stuckTime = stuckTime;
        
        BodyDef def = new BodyDef();
        def.type = BodyType.STATIC;
        def.position = vertex.getLocation();
        
        FixtureDef fovDef = new FixtureDef();
        fovDef.shape = getFOVShape(radius);
        fovDef.isSensor = true;
        fovDef.userData = this;
        
        Body body = scenario.getWorld().createBody(def);
        body.createFixture(fovDef);
    }
    
    @Override
    public int getActPeriod() {
        return 0;
    }
    
    private Shape getFOVShape(float radius) {
        Shape shape = new CircleShape();
        shape.setRadius(radius);
        return shape;
    }
    
    @Override
    public void act() {
        if (scenario.getTime() - lastUpdate > stuckTime) {
            for (Car car : carsInSight) {
                scenario.remove(car);
            }
        }
    }

    @Override
    public void addFixtureInSight(Fixture fixture) {
        if (fixture.getUserData() instanceof Car) {
            carsInSight.add((Car) fixture.getUserData());
            lastUpdate = scenario.getTime();
        }
    }

    @Override
    public void removeFixtureInSight(Fixture fixture) {
        if (fixture.getUserData() instanceof Car) {
            carsInSight.remove((Car) fixture.getUserData());
            lastUpdate = scenario.getTime();
        }
    }
}
