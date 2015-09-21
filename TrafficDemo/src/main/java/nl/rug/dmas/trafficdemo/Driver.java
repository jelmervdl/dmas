/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.HashSet;
import java.util.Set;
import org.jbox2d.collision.shapes.CircleShape;
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
        Vec2 worldMouse = scenario.commonKnowledge.get("mouse");
        Vec2 carMouse = car.body.getLocalPoint(worldMouse);

        if (carMouse.x < -2)
            car.steer = SteerDirection.LEFT;
        else if (carMouse.x > 2)
            car.steer = SteerDirection.RIGHT;
        else
            car.steer = SteerDirection.NONE;

        if (carMouse.y < -2)
            car.acceleration = Acceleration.ACCELERATE;
        else if (carMouse.y > 2)
            car.acceleration = Acceleration.ACCELERATE; //Acceleration.BRAKE;
        else
            car.acceleration = Acceleration.NONE;
    }
    
    public boolean seesOtherCars() {
        for (Fixture fixture : fixturesInSight)
            if (!fixture.isSensor() && fixture.getUserData() instanceof Car)
                return true;
        
        return false;
    }
}
