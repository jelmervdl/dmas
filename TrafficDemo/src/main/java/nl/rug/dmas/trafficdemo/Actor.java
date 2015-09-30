/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

/**
 *
 * @author jelmer
 */
public interface Actor {
    
    /**
     * Determines how often the actor gets a chance to act
     * @return The time between act calls in milliseconds
     */
    public int getActPeriod();
    
    /**
     * If it is time for the actor to act, then that should happen here.
     */
    public void act();
}
