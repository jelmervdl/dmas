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
    
    public void step()
    {
       Vec2 direction = new Vec2();
       
       direction.addLocal(steerToAvoidCars().mul(0.8f));
       
       direction.addLocal(steerTowardsMouse().mul(0.2f));
       
       setSteerDirection(direction);
    }
    
    private void setSteerDirection(Vec2 direction) {
        car.setSteeringDirection(direction.mul(-1f));
        
        car.setSpeedKMH(direction.length() * 10);

        if (direction.length() > 0.5f)
            car.acceleration = Acceleration.ACCELERATE;
        else
            car.acceleration = Acceleration.NONE;
    }
    
    private Vec2 steerToAvoidCars() {
        Vec2 direction = new Vec2(0, 0);
        
        for (Car other : getCarsInSight()) {
            Vec2 directionTowardsCar = car.body.getLocalPoint(other.body.getWorldCenter());
            
            if (directionTowardsCar.length() < 5f)
                direction.addLocal(directionTowardsCar.mul(-1));
        }
        
        return direction;
    }
    
    private Vec2 steerTowardsMouse() {
        Vec2 worldMouse = (Vec2) scenario.commonKnowledge.get("mouse");
        return car.body.getLocalPoint(worldMouse);
    }
    
    private List<Car> getCarsInSight() {
        ArrayList<Car> cars = new ArrayList<>();
        for (Fixture fixture : fixturesInSight)
            if (!fixture.isSensor() && fixture.getUserData() instanceof Car)
                cars.add((Car) fixture.getUserData());
        
        return cars;
    }
    
    public boolean seesOtherCars() {
        return !getCarsInSight().isEmpty();
    }
}
