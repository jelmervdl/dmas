/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Fixture;

/**
 *
 * @author jelmer
 */
public interface Observer {
    public void addFixtureInSight(Fixture fixture);
    
    public void removeFixtureInSight(Fixture fixture);
}
