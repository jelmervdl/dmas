/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.bezier;

import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public class QuadraticBezier extends Bezier {

    public QuadraticBezier(Vec2 source, Vec2 destination) {
        super(source, destination);
    }

    @Override
    protected Vec2 interpolate(float t, Vec2... controlPoints) {
        //        TODO check 0 <= t <= 1
        //        TODO check length controlPoints == 1
        Vec2 control = controlPoints[0];
        Vec2 intermediate1 = new LinearBezier(this.source, control).interpolate(t);
        Vec2 intermediate2 = new LinearBezier(control, this.destination).interpolate(t);
        return intermediate1.mulLocal(1 - t).addLocal(intermediate2.mulLocal(t));
    }
}
