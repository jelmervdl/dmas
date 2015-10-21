/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.List;
import nl.rug.dmas.trafficdemo.Scenario;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class AutonomousDriver extends Driver {
    
    public AutonomousDriver(Scenario scenario, List<Vec2> path, float viewLength) {
        super(scenario, path, viewLength);
    }
    
    @Override
    public int getActPeriod() {
//        return 100;
        return 0;
    }
    
    @Override
    public Shape getFOVShape() {
        Shape shape = new CircleShape();
        shape.setRadius(viewLength);
        return shape;
    }
}
