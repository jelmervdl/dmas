/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import javax.swing.table.AbstractTableModel;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.ScenarioAdapter;
import nl.rug.dmas.trafficdemo.actors.Driver;
import nl.rug.dmas.trafficdemo.streetgraph.PointPath;

/**
 *
 * @author jelmer
 */
public class RouteStatistics extends ScenarioAdapter {
    
    final AbstractAveragingTableModel<PointPath> model = new AbstractAveragingTableModel<>();
    
    @Override
    public void carRemoved(Car car) {
        if (car.getDriver().reachedDestination()) {
            try {
                Driver driver = car.getDriver();
                PointPath path = (PointPath) driver.getPath();
                model.add(path, driver.getDrivingTime());
            } catch (ClassCastException e) {
                // So this car is not what we think it is
            }
        }
    }

    public AbstractTableModel getModel() {
        return model;
    }
    
}
