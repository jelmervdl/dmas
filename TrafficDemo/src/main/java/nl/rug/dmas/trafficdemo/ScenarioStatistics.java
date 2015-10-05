/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import nl.rug.dmas.trafficdemo.actors.AutonomousDriver;
import nl.rug.dmas.trafficdemo.streetgraph.PointPath;

/**
 *
 * @author jelmer
 */
public class ScenarioStatistics extends AbstractTableModel {
    final static int COLUMN_PROPERTY = 0;
    final static int COLUMN_VALUE = 1;
    
    List<PointPath> paths = new ArrayList<>();
    List<Counter> counters = new ArrayList<>();

    public void carAdded(Car car) {
        // Do nothing for now :(
    }
    
    public void carRemoved(Car car) {
        if (car.getDriver().reachedDestination()) {
            try {
                AutonomousDriver driver = (AutonomousDriver) car.getDriver();
                PointPath path = (PointPath) driver.getPath();
                int index = paths.indexOf(path);
                if (index == -1) {
                    paths.add(path);
                    counters.add(new Counter(1));
                    fireTableRowsInserted(paths.size() - 1, paths.size());
                } else {
                    Counter counter = counters.get(index);
                    counter.increment();
                    fireTableCellUpdated(index, COLUMN_VALUE);
                }
            } catch (ClassCastException e) {
                // So this car is not what we think it is
            }
        }
    }
    
    public String getPropertyName(int row) {
        return row < paths.size() ? paths.get(row).toString() : "";
    }
    
    public Long getPropertyValue(int row) {
        return row < paths.size() ? counters.get(row).getValue() : 0l;
    }

    @Override
    public int getRowCount() {
        return paths.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case COLUMN_PROPERTY:
                return "Property";
            case COLUMN_VALUE:
                return "Value";
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLUMN_PROPERTY:
                return String.class;
            case COLUMN_VALUE:
                return Long.class;
            default:
                return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COLUMN_PROPERTY:
                return getPropertyName(rowIndex);
            case COLUMN_VALUE:
                return getPropertyValue(rowIndex);
            default:
                return null;
        }
    }

    private static class Counter {
        long value;
        
        public Counter(long value) {
            this.value = value;
        }
        
        public void increment() {
            value += 1;
        }
        
        public long getValue() {
            return value;
        }
    }
}
