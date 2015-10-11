/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import javax.swing.table.AbstractTableModel;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.ScenarioAdapter;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class CollisionStatistics extends ScenarioAdapter {
    
    final AbstractCountingTableModel<String> model = new AbstractCountingTableModel<>();
    
    @Override
    public void carsCollided(Car carA, Car carB, Vec2 localPosition) {
        model.increment(String.format("%s - %s", getDriverType(carA), getDriverType(carB)));
    }
    
    private String getDriverType(Car car) {
        return car.getDriver().getClass().getSimpleName();
    }
    
    public AbstractTableModel getModel() {
        return model;
    }
}
