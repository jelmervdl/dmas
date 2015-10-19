/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.List;
import nl.rug.dmas.trafficdemo.Acceleration;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.ShapeUtil;
import nl.rug.dmas.trafficdemo.VecUtils;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

/**
 *
 * @author lauravandebraak
 */
public class HumanDriver extends Driver{
    private float viewLength;
    private int actPeriod;
    
    public HumanDriver(Scenario scenario, float view) {
        super(scenario);
        this.viewLength = view;
    }
    
    public HumanDriver(Scenario scenario, List<Vec2> path, float view, int actPeriod) {
        super(scenario, path);
        this.viewLength = view;
        this.actPeriod = actPeriod;
    }

    /**
     * Get Field of view shape
     * @return shape for field of view
     */
    @Override
    public Shape getFOVShape() {
        return ShapeUtil.createArc(viewLength, 3 * MathUtils.QUARTER_PI, 1 * MathUtils.QUARTER_PI, car.getDriverPosition());
    }
    
    @Override
    public boolean reachedDestination() {
        return path != null && pathIndex == path.size();
    }

    @Override
    public int getActPeriod() {
        return actPeriod;
    }

    /**
     * Update the steering direction of the car we drive
     */
    
    //todo: update
    @Override
    public void act()
    {
        debugDraw.clear();
        
        if (steerTowardsPath().length() == 0)
        float steeringAngle = VecUtils.getAngle(steerTowardsPath().negate());
        
        car.setSteeringDirection(steeringAngle);
            car.setAcceleration(Acceleration.NONE);
        else if (speedAdjustmentToAvoidCars() < 0)
            car.setAcceleration(Acceleration.BRAKE);
        else if (car.getSpeedKMH() < 30)
            car.setAcceleration(Acceleration.ACCELERATE);
        else
            car.setAcceleration(Acceleration.NONE);
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
