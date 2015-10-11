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

/**
 *
 * @author jelmer
 */
public class DriverTimeStatistics extends ScenarioAdapter {

    final AbstractAveragingTableModel<String> model = new AbstractAveragingTableModel<>();

    @Override
    public void carRemoved(Car car) {
        if (car.getDriver().reachedDestination()) {
            Driver driver = car.getDriver();
            model.add(driver.getClass().getSimpleName(),
                    driver.getScenario().getTime() - driver.getTimeOfCreation());
        }
    }

    public AbstractTableModel getModel() {
        return model;
    }
}
