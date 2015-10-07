/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.ScenarioListener;

/**
 *
 * @author jelmer
 */
public class DriverTypeStatistics extends AbstractCountingTableModel<String> implements ScenarioListener {

    @Override
    public void carAdded(Car car) {
        //
    }

    @Override
    public void carRemoved(Car car) {
        if (car.getDriver().reachedDestination())
            increment(car.getDriver().getClass().getSimpleName());
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
