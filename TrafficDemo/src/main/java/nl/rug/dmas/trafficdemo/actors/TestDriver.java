/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

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
     * Behavior that determines the direction towards the mouse. The mouse
     * location is written to the scenario's common knowledge table every update
     * @return a vector direction (with length!)
     */
    private Vec2 steerTowardsMouse() {
        Vec2 worldMouse = (Vec2) scenario.getCommonKnowledge().get("mouse");
        return car.getLocalPoint(worldMouse);
    }

    @Override
    public boolean reachedDestination() {
        return false;
    }
}
