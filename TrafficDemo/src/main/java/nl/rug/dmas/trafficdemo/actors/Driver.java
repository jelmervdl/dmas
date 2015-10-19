/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rug.dmas.trafficdemo.Actor;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.DebugGraphicsQueue;
import nl.rug.dmas.trafficdemo.Observer;
import nl.rug.dmas.trafficdemo.Scenario;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 * Abstract class that represents a driver. Missing is the act() method that
 * controls the actual behaviour. This abstract implementation offers a few
 * utilities such as getCarsInSight() for the implementations.
 * @author jelmer
 */
abstract public class Driver implements Actor, Observer {
    final protected Scenario scenario;
    
    protected Car car;
    
    final List<Vec2> path;
    
    int pathIndex = 0;
    
    // A set of all fixtures in the field of view. It is updated by the
    // scenario step function itself, drivers (actors) don't have access
    // to anything outside of this list and the public getters of Scenario.
    final protected Set<Fixture> fixturesInSight = new HashSet<>();
    
    final public DebugGraphicsQueue debugDraw = new DebugGraphicsQueue();
    
    final private float timeOfCreation;
    
    final float skipAheadPointDistance = 3.0f;
    
    final float moveToNextPointDistance = 5.0f;
    
    /**
     * Create a driver that will drive along a predefined route of positions
     * @param scenario
     * @param path List of Vec2's in world coordinates
     */
    public Driver(Scenario scenario, List<Vec2> path) {
        this.scenario = scenario;
        this.path = path;
        this.timeOfCreation = scenario.getTime();
    }
    
    /**
     * Create a blank driver in a scenario.
     * @param scenario scenario in which the driver is driving.
     */
    public Driver(Scenario scenario) {
        this(scenario, (CopyOnWriteArrayList<Vec2>) scenario.getCommonKnowledge().get("path"));
    }
    
    /**
     * Called by Car when a Driver is passed in
     * @param car 
     */
    public void setCar(Car car) {
        this.car = car;
    }
    
    /**
     * Should return a shape that represents the field of view of the driver.
     * @return 
     */
    public Shape getFOVShape() {
        Shape shape = new CircleShape();
        shape.setRadius(8);
        return shape;
    }
    
    public List<Vec2> getPath() {
        return path;
    }
    
    public int getPathIndex() {
        return pathIndex;
    }
    
    public float getDrivingTime() {
        return scenario.getTime() - timeOfCreation;
    }

    /**
     * Called when a Fixture enters our FOV shape in the world.
     * Callback for the Observer interface.
     * @param fixture
     */
    @Override
    public void addFixtureInSight(Fixture fixture) {
        fixturesInSight.add(fixture);
    }

    /**
     * Called when a Fixture leaves our FOV shape in the world.
     * Callback for the Observer interface.
     * @param fixture
     */
    @Override
    public void removeFixtureInSight(Fixture fixture) {
        fixturesInSight.remove(fixture);
    }
    
    /**
     * Filters the list of fixtures in sight and only returns actual cars that
     * are not my own car.
     * @return a list of cars inside the FOV.
     */
    protected List<Car> getCarsInSight() {
        ArrayList<Car> cars = new ArrayList<>();
        for (Fixture fixture : fixturesInSight)
            if (!fixture.isSensor()
                    && fixture.getUserData() instanceof Car
                    && fixture.getUserData() != car)
                cars.add((Car) fixture.getUserData());
        
        return cars;
    }
    
    protected Vec2 steerTowardsPath() {
        if (path != null) {
            // first, try to find the closest point
            for (int i = pathIndex; i < path.size(); ++i) {
                Vec2 directionToNextPoint = car.getDriverPoint(path.get(i));
                
                // but stop after the first one because we shouldn't skip too much
                if (directionToNextPoint.length() < skipAheadPointDistance) {
                    pathIndex = i;
                    break;
                }
            }
            
            // then try to find the next point far way enough from the car to 
            // drive towards
            while (pathIndex < path.size()) {
                Vec2 directionToNextPoint = car.getDriverPoint(path.get(pathIndex));
                
                if (directionToNextPoint.length() > moveToNextPointDistance) {
                    return directionToNextPoint;
                } else {
                    pathIndex += 1;
                }
            }
        }
        
        return new Vec2(0, 0);
    }

    /**
     * Test whether the destination is reached. This method is used by the sinks
     * to determine whether a car can be removed from the scenario.
     * @return whether the destination is reached right now
     */
    public boolean reachedDestination() {
        return path != null && pathIndex == path.size();
    }
}
