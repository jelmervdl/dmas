/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.List;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.ShapeUtil;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

/**
 *
 * @author lauravandebraak
 */
public class HumanDriver extends Driver{
    private int actPeriod;
    
    public HumanDriver(Scenario scenario, List<Vec2> path, float viewLength, int actPeriod) {
        super(scenario, path, viewLength);
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
    public int getActPeriod() {
        return actPeriod;
    }
}
