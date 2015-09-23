/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 *
 * @author jelmer
 */
class Driver {
    Scenario scenario;
    
    Car car;
    
    // A set of all fixtures in the 
    Set<Fixture> fixturesInSight = new HashSet<>();
    
    public Driver(Scenario scenario) {
        this.scenario = scenario;
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
    
    /**
     * Update the steering direction of the car we drive
     */
    public void step()
    {
       Vec2 direction = new Vec2();
       
       direction.addLocal(steerToAvoidCars().mul(0.8f));
       
       direction.addLocal(steerTowardsMouse().mul(0.2f));
       
       setSteerDirection(direction);
    }
    
    /**
     * Set the speed and steering direction. This implementation right now uses
     * a vector with a length and an angle to determine the speed and direction.
     * @param direction vector from the current location to the goal location
     */
    private void setSteerDirection(Vec2 direction) {
        car.setSteeringDirection(direction.mul(-1f));
        
        car.setSpeedKMH(direction.length() * 5);

        if (direction.length() > 0.5f)
            car.acceleration = Acceleration.ACCELERATE;
        else
            car.acceleration = Acceleration.NONE;
    }
    
    /**
     * Behavior that determines the optimal direction to avoid other cars
     * @return a vector direction
     */
    private Vec2 steerToAvoidCars() {
        Vec2 direction = new Vec2(0, 0);
        
        for (Car other : getCarsInSight()) {
            Vec2 directionTowardsCar = car.body.getLocalPoint(other.body.getWorldCenter());
            
            if (directionTowardsCar.length() < 5f) {
                // todo: Right now, cars that are far away have more impact on
                // the direction sum than cars close by. Maybe that should be
                // turned around.
                direction.addLocal(directionTowardsCar.negate());
            }
        }
        
        return direction;
    }
    
    /**
     * Behavior that determines the direction towards the mouse. The mouse
     * location is written to the scenario's common knowledge table every update
     * @return a vector direction (with length!)
     */
    private Vec2 steerTowardsMouse() {
        Vec2 worldMouse = (Vec2) scenario.commonKnowledge.get("mouse");
        return car.body.getLocalPoint(worldMouse);
    }
    
    /**
     * Filters the list of fixtures in sight and only returns actual cars.
     * @return a list of cars inside the FOV.
     */
    private List<Car> getCarsInSight() {
        ArrayList<Car> cars = new ArrayList<>();
        for (Fixture fixture : fixturesInSight)
            if (!fixture.isSensor() && fixture.getUserData() instanceof Car)
                cars.add((Car) fixture.getUserData());
        
        return cars;
    }
    
    /**
     * Utility function that returns true if this cars sees other cars. Used by
     * TrafficPanel to change the color of the FOV (if turned on).
     * @return whether this car has other cars in its FOV.
     */
    public boolean seesOtherCars() {
        return !getCarsInSight().isEmpty();
    }
}
