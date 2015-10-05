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
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

/**
 *
 * @author lauravandebraak
 */
public class HumanDriver extends Driver{
    final private List<Vec2> path;
    
    private double viewAngle = 40;
    private float viewLength = 8;
    
    private int pathIndex = 0;

    public HumanDriver(Scenario scenario) {
        super(scenario);
        this.path = (CopyOnWriteArrayList<Vec2>) scenario.getCommonKnowledge().get("path");
    }
    
    public HumanDriver(Scenario scenario, List<Vec2> path) {
        super(scenario);
        this.path = path;
    }
    
    
    private double sinAngle(){
        double angle = Math.toRadians(getViewAngle());
        angle = Math.sin(angle);
        return Math.toDegrees(angle);
    }
    
    private double cosAngle(){
        double angle = Math.toRadians(getViewAngle());
        angle = Math.cos(angle);
        return Math.toDegrees(angle);
    }
    
//    /**
//     * Get field of view shape of the driver, an arc
//     * @return field of view
//     */
//    @Override
//    public Shape getFOVShape() {
//        PolygonShape shape = new PolygonShape();
//        float rotateAngle = (float) (-0.25 * Math.PI);
//        float length = (float) (getViewLength() / 100);
//        float startHeight =(float) (cosAngle() * length);
//        float topHeight = (float) (length - startHeight);
//        float carWidth = (float) (car.getWidth() / 4);
//        float width = (float) (sinAngle() * length) + carWidth;
//        //Vec2[] vertices = {new Vec2(width, startHeight), new Vec2(-width, topHeight), new Vec2(-width, -topHeight),new Vec2(width, -startHeight)};
//        Vec2[] vertices = {
//            VecUtils.rotate(new Vec2(-carWidth, 0), rotateAngle),
//            VecUtils.rotate(new Vec2(-width, startHeight), rotateAngle), 
//            VecUtils.rotate(new Vec2(0, length), rotateAngle), 
//            VecUtils.rotate(new Vec2(width, startHeight), rotateAngle), 
//            VecUtils.rotate(new Vec2(carWidth, 0), rotateAngle),
//            VecUtils.rotate(new Vec2(width, -startHeight), rotateAngle),
//            VecUtils.rotate(new Vec2(0, -length), rotateAngle),
//            VecUtils.rotate(new Vec2(-width, -startHeight), rotateAngle)
//        };
//        shape.set(vertices,8);
//        return shape;
//    }

    /**
     * Get Field of view shape
     * @return shape for field of view
     */
    @Override
    public Shape getFOVShape() {
        PolygonShape shape  = new PolygonShape();
        shape.setAsBox(viewLength/2, viewLength/2, new Vec2(0,car.getLength()), (float) 90); //todo fix orientation
        return shape;
    }
    
    @Override
    public boolean reachedDestination() {
        return path != null && pathIndex == path.size();
    }

    @Override
    public int getActPeriod() {
        return 0;
    }

    /**
     * Update the steering direction of the car we drive
     */
    
    //todo: update
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

    /**
     * @return the viewAngle
     */
    public double getViewAngle() {
        return viewAngle;
    }

    /**
     * @param viewAngle the viewAngle to set
     */
    public void setViewAngle(double viewAngle) {
        this.viewAngle = viewAngle;
    }

    /**
     * @return the viewLength
     */
    public float getViewLength() {
        return viewLength;
    }

    /**
     * @param viewLength the viewLength to set
     */
    public void setViewLength(float viewLength) {
        this.viewLength = viewLength;
    }
    
    
}
