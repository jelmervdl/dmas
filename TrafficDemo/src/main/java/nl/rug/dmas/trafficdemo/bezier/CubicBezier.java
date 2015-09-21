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
public class CubicBezier extends Bezier {

    public CubicBezier(Vec2 source, Vec2 destination) {
        super(source, destination);
    }

    @Override
    protected Vec2 interpolate(float t, Vec2... controlPoints) {
//        TODO check 0 <= t <= 1
//        TODO check length controlPoints == 2
        Vec2 sourceControl = controlPoints[0];
        Vec2 destinationControl = controlPoints[1];
        Vec2 intermediate1 = new QuadraticBezier(this.source, destinationControl).interpolate(t, sourceControl);
        Vec2 intermediate2 = new QuadraticBezier(sourceControl, this.destination).interpolate(t, destinationControl);

        return intermediate1.mulLocal(t).addLocal(intermediate2.mulLocal(t));
    }
}
