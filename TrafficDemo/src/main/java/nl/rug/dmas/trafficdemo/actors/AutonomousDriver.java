/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rug.dmas.trafficdemo.Acceleration;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.VecUtils;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class AutonomousDriver extends Driver {
    public AutonomousDriver(Scenario scenario) {
        super(scenario);    
    }
    
    public AutonomousDriver(Scenario scenario, List<Vec2> path) {
        super(scenario, path);
    }
    
    @Override
    public int getActPeriod() {
//        return 100;
        return 0;
    }
    
    @Override
    public Shape getFOVShape() {
        Shape shape = new CircleShape();
        shape.setRadius(12);
        return shape;
    }
    
    /**
     * Update the steering direction of the car we drive
     */
    @Override
    public void act()
    {
        debugDraw.clear();
        
        car.setSteeringDirection(steerTowardsPath().negate());
       
        if (steerTowardsPath().length() == 0)
            car.setAcceleration(Acceleration.NONE);
        else if (speedAdjustmentToAvoidCars() < 0)
            car.setAcceleration(Acceleration.BRAKE);
        else if (car.getSpeedKMH() < 30)
            car.setAcceleration(Acceleration.ACCELERATE);
        else
            car.setAcceleration(Acceleration.NONE);
    }
    
    /**
     * Behaviour that determines the optimal direction to avoid other cars
     * @return a vector direction
     */
    private Vec2 steerToAvoidCars() {
        Vec2 direction = new Vec2(0, 0);
        
        return direction;
    }
    
    private float speedAdjustmentToAvoidCars() {
        VecUtils.Intersection mostImportant = null;
        
//        debugDraw.draw(car.getPosition(), car.getAbsoluteVelocity());
            
        for (Car other : getCarsInSight()) {
            // If that car is driving towards me, well, shit!
            
            debugDraw.draw(other.getPosition(), other.getAbsoluteVelocity());
            
            VecUtils.Intersection intersection = VecUtils.intersect(car.getPosition(), car.getAbsoluteVelocity(),
                other.getPosition(), other.getAbsoluteVelocity());
            
            if (intersection == null)
                continue;
            
            debugDraw.draw(intersection.position);
            
            if (mostImportant == null || Math.abs(mostImportant.u - mostImportant.v) > Math.abs(intersection.u - intersection.v))
                mostImportant = intersection;
        }
        
        if (mostImportant == null)
            return 0;
        
        // Time until I reach the intersection minus the time the other
        // reaches the intersection
        float d = mostImportant.v - mostImportant.u;

        // If there is enough time to pass safely, ignore this car
        // Todo: determine 3 using the length of our and the other car and
        // their speed. Hard math ahead?
        //if (Math.abs(d) > 4)
        //    return 0;
        
        // d < 0: I'm there first, we should speed up a bit maybe?
        // d > 0: other is there first, we should brake?
        return d;
    }
}
