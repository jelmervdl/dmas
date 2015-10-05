/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public abstract class Sign {
    Vec2 location;

    public Sign(Vec2 location) {
        this.location = location;
    }

}
