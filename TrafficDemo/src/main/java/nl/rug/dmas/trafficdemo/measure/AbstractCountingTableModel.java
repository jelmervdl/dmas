package nl.rug.dmas.trafficdemo.measure;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jelmer
 * @param <K> Key
 */
public class AbstractCountingTableModel<K> extends AbstractMapTableModel<K, Counter> {
    public void increment(K key) {
        int index = indexOf(key);
        if (index == -1) {
            index = put(key, new Counter(1));
            fireTableRowsInserted(index, index + 1);
        } else {
            get(index).increment();
            fireTableCellUpdated(index, COLUMN_VALUE);
        }
    }
    
    public void decrement(K key) {
        int index = indexOf(key);
        if (index == -1) {
            index = put(key, new Counter(-1));
            fireTableRowsInserted(index, index + 1);
        } else {
            get(index).decrement();
            fireTableCellUpdated(index, COLUMN_VALUE);
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLUMN_VALUE)
            return get(rowIndex).getValue();
        else
            return super.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == COLUMN_VALUE)
            return Long.class;
        else
            return super.getColumnClass(columnIndex);
    }
    
    
}
