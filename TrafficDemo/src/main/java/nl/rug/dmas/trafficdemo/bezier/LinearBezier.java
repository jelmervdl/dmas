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
public class LinearBezier extends Bezier {

    public LinearBezier(Vec2 source, Vec2 destination) {
        super(source, destination);
    }

    @Override
    protected Vec2 interpolate(float t, Vec2... controlPoints) {
        //        TODO check 0 <= t <= 1
        //        TODO check length controlPoints == 1
        return this.source.mul(1 - t).add(this.destination.mul(t));
    }
}
