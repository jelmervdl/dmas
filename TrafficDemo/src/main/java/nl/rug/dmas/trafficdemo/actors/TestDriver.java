/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import nl.rug.dmas.trafficdemo.Acceleration;
import nl.rug.dmas.trafficdemo.Scenario;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class TestDriver extends Driver {

    public TestDriver(Scenario scenario) {
        super(scenario);
    }
    
    @Override
    public void act() {
        setSteerDirection(steerTowardsMouse());
    }
    
    /**
     * Behaviour that determines the direction towards the mouse. The mouse
     * location is written to the scenario's common knowledge table every update
     * @return a vector direction (with length!)
     */
    private Vec2 steerTowardsMouse() {
        Vec2 worldMouse = (Vec2) scenario.getCommonKnowledge().get("mouse");
        return car.getLocalPoint(worldMouse);
    }
    
    /**
     * Set the speed and steering direction. This implementation right now uses
     * a vector with a length and an angle to determine the speed and direction.
     * @param direction vector from the current location to the goal location
     */
    protected void setSteerDirection(Vec2 direction) {
        car.setSteeringDirection(direction.mul(-1f));
        
        car.setSpeedKMH(direction.length() * 5);

        if (direction.length() > 0.5f)
            car.setAcceleration(Acceleration.ACCELERATE);
        else
            car.setAcceleration(Acceleration.NONE);
    }
    
    @Override
    public boolean reachedDestination() {
        return false;
    }
}
