/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jelmer
 * @param <K> Key
 * @param <V> Value
 */
public class AbstractMapTableModel<K,V> extends AbstractTableModel {
    final static public int COLUMN_PROPERTY = 0;
    final static public int COLUMN_VALUE = 1;
    
    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();
    
    public int put(K key, V value) {
        keys.add(key);
        values.add(value);
        return keys.size() - 1;
    }
    
    public int indexOf(K key) {
        return keys.indexOf(key);
    }
    
    public V get(int index) {
        return values.get(index);
    }
    
    public K getKey(int index) {
        return keys.get(index);
    }

    @Override
    public int getRowCount() {
        return keys.size();
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= getRowCount())
            return null;
        
        switch (columnIndex) {
            case COLUMN_PROPERTY:
                return getKey(rowIndex);
            case COLUMN_VALUE:
                return get(rowIndex);
            default:
                return null;
        }
    }
}
