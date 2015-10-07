/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.ScenarioListener;
import nl.rug.dmas.trafficdemo.actors.AutonomousDriver;
import nl.rug.dmas.trafficdemo.actors.Driver;
import nl.rug.dmas.trafficdemo.streetgraph.PointPath;

/**
 *
 * @author jelmer
 */
public class RouteStatistics extends AbstractCountingTableModel<PointPath> implements ScenarioListener {
    
    @Override
    public void carAdded(Car car) {
        // Do nothing for now :(
    }
    
    @Override
    public void carRemoved(Car car) {
        if (car.getDriver().reachedDestination()) {
            try {
                Driver driver = car.getDriver();
                PointPath path = (PointPath) driver.getPath();
                increment(path);
            } catch (ClassCastException e) {
                // So this car is not what we think it is
            }
        }
    }
    
    @Override
    public void scenarioStarted() {
        // Do nothing, it can be started multiple times!
    }
    
    @Override
    public void scenarioStepped() {
        //
    }
    
    @Override
    public void scenarioStopped() {
        //
    }

    @Override
    public void selectionChanged() {
        //
    }
}
