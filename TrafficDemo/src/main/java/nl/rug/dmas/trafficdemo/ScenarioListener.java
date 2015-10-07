/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

/**
 *
 * @author jelmer
 */
public interface ScenarioListener
{
    public void carAdded(Car car);
    
    public void carRemoved(Car car);
    
    public void scenarioStepped();
    
    public void selectionChanged();
}
