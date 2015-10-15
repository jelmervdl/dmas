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
    
    /**
     * Behaviour that determines the optimal direction to avoid other cars
     * @return a vector direction
     */
    private Vec2 steerToAvoidCars() {
        Vec2 direction = new Vec2(0, 0);
        
        return direction;
    }
    
    private float speedAdjustmentToPreventColission(float steeringAngle, float rayLength, int numberOfRays) {
        float originStart = -car.getWidth() / 2;
        float originEnd = car.getWidth() / 2;
        float originStep = (originEnd - originStart) / (numberOfRays - 1);
        
        float originY = -car.getLength() / 2; // front of the car
        
        // The arcs bent with the steering of the car
        float arcStart = MathUtils.min(2.5f * MathUtils.QUARTER_PI - steeringAngle + MathUtils.HALF_PI, MathUtils.PI);
        float arcStop = MathUtils.max(1.5f * MathUtils.QUARTER_PI - steeringAngle + MathUtils.HALF_PI, 0);
        float arcStep = (arcStop - arcStart) / (numberOfRays - 1);
        
        final AtomicReference<Float> hit = new AtomicReference<>();
        
        for (int i = 0; i < numberOfRays; ++i) {
            final float angle = arcStart + i * arcStep;
            final Vec2 origin = car.getWorldPoint(new Vec2(originStart + i * originStep, originY));
            final Vec2 direction = car.getWorldVector(new Vec2(rayLength * MathUtils.cos(angle), rayLength * -MathUtils.sin(angle)));
            
            debugDraw.draw(origin, direction);
            
            scenario.getWorld().raycast(new RayCastCallback() {
                @Override
                public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
                    if (!(fixture.getUserData() instanceof Car))
                        return 1;
                    
                    // I have to make copies of point and normal as they are
                    // reused by raycast() for the next call to the callback.
                    debugDraw.draw(new Vec2(point), new Vec2(normal));
                    
                    float dist = point.sub(origin).length();
                    if (hit.get() == null || hit.get() > dist) {
                        hit.set(dist);
                    }
                    
                    return fraction;
                }
            }, origin, origin.add(direction));
        }
        
        return hit.get() == null ? 0 : hit.get();
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
