/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import nl.rug.dmas.trafficdemo.measure.DriverTypeStatistics;
import nl.rug.dmas.trafficdemo.measure.RouteStatistics;

/**
 *
 * @author jelmer
 */
public class StatisticsWindow extends JFrame {
    final Scenario scenario;
    
    public StatisticsWindow(Scenario scenario) {
        this.scenario = scenario;
        
        setPreferredSize(new Dimension(300, 300));
        setTitle("Statistics");
        setType(Type.UTILITY);
        
        Container content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        JLabel routeLabel = new JLabel("Route:");
        routeLabel.setAlignmentY(LEFT_ALIGNMENT);
        RouteStatistics routeStats = new RouteStatistics();
        scenario.addListener(routeStats);
        content.add(routeLabel);
        content.add(new JTable(routeStats));
        
        DriverTypeStatistics driverTypeStats = new DriverTypeStatistics();
        scenario.addListener(driverTypeStats);
        content.add(new JLabel("Driver:"));
        content.add(new JTable(driverTypeStats));
        
        pack();
    }
}
