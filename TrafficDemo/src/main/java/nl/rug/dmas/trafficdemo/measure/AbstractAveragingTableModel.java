/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jelmer
 * @param <K> Key
 *
 */
public class AbstractAveragingTableModel<K> extends AbstractMapTableModel<K, List<Float>> {

    static public final int COLUMN_AVERAGE = 2;

    public void add(K key, float value) {
        int index = indexOf(key);
        if (index == -1) {
            List<Float> list = new ArrayList<>();
            list.add(value);
            index = put(key, list);
            fireTableRowsInserted(index, index + 1);
        } else {
            get(index).add(value);
            fireTableRowsUpdated(index, index);
        }
    }

    public int getCount(int rowIndex) {
        return get(rowIndex).size();
    }

    public float getAverage(int rowIndex) {
        List<Float> values = get(rowIndex);
        float sum = 0;

        for (float value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == COLUMN_VALUE) {
            return "Count";
        } else if (columnIndex == COLUMN_AVERAGE) {
            return "Average";
        } else {
            return super.getColumnName(columnIndex);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLUMN_VALUE) {
            return getCount(rowIndex);
        } else if (columnIndex == COLUMN_AVERAGE) {
            return getAverage(rowIndex);
        } else {
            return super.getValueAt(rowIndex, columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == COLUMN_VALUE) {
            return Integer.class;
        } else if (columnIndex == COLUMN_AVERAGE) {
            return Float.class;
        } else {
            return super.getColumnClass(columnIndex);
        }
    }
}
