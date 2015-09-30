/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.Scenario;
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
    
    /**
     * Update the steering direction of the car we drive
     */
    @Override
    public void act()
    {
       Vec2 direction = new Vec2();
       
       direction.addLocal(steerToAvoidCars().mul(0.8f));
       
       direction.addLocal(steerTowardsPath().mul(0.2f));
       
       setSteerDirection(direction);
    }
    
    /**
     * Behavior that determines the optimal direction to avoid other cars
     * @return a vector direction
     */
    private Vec2 steerToAvoidCars() {
        Vec2 direction = new Vec2(0, 0);
        
        for (Car other : getCarsInSight()) {
            Vec2 directionTowardsCar = car.getLocalPoint(other.getPosition());
            
            if (directionTowardsCar.length() < 5f) {
                // todo: Right now, cars that are far away have more impact on
                // the direction sum than cars close by. Maybe that should be
                // turned around.
                direction.addLocal(directionTowardsCar.negate());
            }
        }
        
        return direction;
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
}
