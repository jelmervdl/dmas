/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.rug.dmas.trafficdemo.Acceleration;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.VecUtils;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 *
 * @author jelmer
 */
public class AutonomousDriver extends Driver {
    
    private int stuck = 0;
    
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
        
        float steeringAngle = VecUtils.getAngle(steerTowardsPath().negate());
        
        car.setSteeringDirection(steeringAngle);
        
        float distance = speedAdjustmentToPreventColission(steeringAngle, 2.0f, 10);
        
        if (distance > 0 && distance < 1.0f && car.getLocalVelocity().y > 0.0f && stuck > 5) {
            car.setAcceleration(Acceleration.REVERSE);
            stuck++;
        } else if (distance > 0) { // && distance < 2.0f, but that is implicit
            car.setAcceleration(Acceleration.BRAKE);
            stuck++;
        } else if (steerTowardsPath().length() == 0) {
            car.setAcceleration(Acceleration.NONE);
            stuck = 0;
        } else if (speedAdjustmentToAvoidCars() < 0) {
            car.setAcceleration(Acceleration.BRAKE);
            stuck = 0;
        } else if (car.getSpeedKMH() < 30) {
            car.setAcceleration(Acceleration.ACCELERATE);
            stuck = 0;
        }
        else {
            car.setAcceleration(Acceleration.NONE);
            stuck = 0;
        }
    }
}
