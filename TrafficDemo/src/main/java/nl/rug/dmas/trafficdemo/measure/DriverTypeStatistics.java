/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import javax.swing.table.AbstractTableModel;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.ScenarioAdapter;

/**
 *
 * @author jelmer
 */
public class DriverTypeStatistics extends ScenarioAdapter {

    final AbstractCountingTableModel<String> model = new AbstractCountingTableModel<>();
    
    @Override
    public void carRemoved(Car car) {
        if (car.getDriver().reachedDestination())
            model.increment(car.getDriver().getClass().getSimpleName());
    }

    public AbstractTableModel getModel() {
        return model;
    }
    
}
