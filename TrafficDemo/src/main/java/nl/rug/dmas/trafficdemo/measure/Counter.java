/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.measure;

/**
 *
 * @author jelmer
 */
public class Counter {
    private long value;

    public Counter(long value) {
        this.value = value;
    }

    public void increment() {
        value += 1;
    }
    
    public void decrement() {
        value -= 1;
    }

    public long getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return Long.toString(getValue());
    }
}
