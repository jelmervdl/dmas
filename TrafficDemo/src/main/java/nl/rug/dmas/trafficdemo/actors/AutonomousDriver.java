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
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class AutonomousDriver extends Driver {
    final private List<Vec2> path;
    
    private int pathIndex = 0;
    
    public AutonomousDriver(Scenario scenario) {
        super(scenario);
        this.path = (CopyOnWriteArrayList<Vec2>) scenario.getCommonKnowledge().get("path");
    }
    
    public AutonomousDriver(Scenario scenario, List<Vec2> path) {
        super(scenario);
        this.path = path;
    }
    
    @Override
    public int getActPeriod() {
        return 100;
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
       
        car.setSpeedKMH(15 + speedAdjustmentToAvoidCars() * 5);
        
        if (steerTowardsPath().length() == 0)
            car.setAcceleration(Acceleration.NONE);
        else if (speedAdjustmentToAvoidCars() < 0)
            car.setAcceleration(Acceleration.BRAKE);
        else
            car.setAcceleration(Acceleration.ACCELERATE);
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
        Intersection mostImportant = null;
        
//        debugDraw.draw(car.getPosition(), car.getAbsoluteVelocity());
            
        for (Car other : getCarsInSight()) {
            // If that car is driving towards me, well, shit!
            
            debugDraw.draw(other.getPosition(), other.getAbsoluteVelocity());
            
            Intersection intersection = intersect(car.getPosition(), car.getAbsoluteVelocity(),
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
        float d = mostImportant.u - mostImportant.v;

        // If there is enough time to pass safely, ignore this car
        // Todo: determine 3 using the length of our and the other car and
        // their speed. Hard math ahead?
        //if (Math.abs(d) > 4)
        //    return 0;
        
        // d < 0: I'm there first, we should speed up a bit maybe?
        // d > 0: other is there first, we should brake?
        return d;
    }
    
    static private class Intersection
    {
        final public float u, v;
        
        final public Vec2 position;
        
        public Intersection(float u, float v, Vec2 position) {
            this.u = u;
            this.v = v;
            this.position = position;
        }
    }
    
    /**
     * Calculate whether two moving objects are going to intersect, and when.
     * @param as Position of a
     * @param ad Velocity of a
     * @param bs Position of b
     * @param bd Velocity of b
     * @return the moment and position of intersection, or null if there is no intersection
     */
    private Intersection intersect(Vec2 as, Vec2 ad, Vec2 bs, Vec2 bd) {
        // Let's try to solve:
        // p = as + ad * u
        // p = bs + bd * v
        
        float dx = bs.x - as.x;
        float dy = bs.y - as.y;
        float det = bd.x * ad.y - bd.y * ad.x;
        float u = (dy * bd.x - dx * bd.y) / det;
        float v = (dy * ad.x - dx * ad.y) / det;
        
        if (u > 0 && v > 0)
            return new Intersection(u, v, as.mul(u));
        else
            return null;
    }
    
    private Vec2 steerTowardsPath() {
        if (path != null) {
            while (pathIndex < path.size()) {
                Vec2 directionToNextPoint = car.getLocalPoint(path.get(pathIndex));

                if (directionToNextPoint.length() > 3.0f) {
                    return directionToNextPoint;
                } else {
                    pathIndex += 1;
                }
            }
        }
        
        return new Vec2(0, 0);
    }

    @Override
    public boolean reachedDestination() {
        return path != null && pathIndex == path.size();
    }
}
