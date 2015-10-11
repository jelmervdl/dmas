/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import nl.rug.dmas.trafficdemo.measure.CollisionStatistics;
import nl.rug.dmas.trafficdemo.measure.DriverTypeStatistics;
import nl.rug.dmas.trafficdemo.measure.RouteStatistics;

/**
 *
 * @author jelmer
 */
public class StatisticsWindow extends JFrame {
    final Scenario scenario;
    
    final Map<String, AbstractTableModel> statistics = new LinkedHashMap<>(); 
    
    public StatisticsWindow(Scenario scenario) {
        this.scenario = scenario;
        
        setPreferredSize(new Dimension(300, 300));
        setTitle("Statistics");
        setType(Type.UTILITY);
        
        Container content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        
        RouteStatistics routeStats = new RouteStatistics();
        scenario.addListener(routeStats);
        statistics.put("Route", routeStats.getModel());
        
        DriverTypeStatistics driverStats = new DriverTypeStatistics();
        scenario.addListener(driverStats);
        statistics.put("Driver types", driverStats.getModel());
        
        CollisionStatistics collisionStats = new CollisionStatistics();
        scenario.addListener(collisionStats);
        statistics.put("Collisions", collisionStats.getModel());
        
        for (Map.Entry<String, AbstractTableModel> statistic : statistics.entrySet()) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(4, 4));
            panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            
            JLabel label = new JLabel(statistic.getKey());
            label.setFont(label.getFont().deriveFont(14f));
            label.setAlignmentY(LEFT_ALIGNMENT);
            panel.add(label, BorderLayout.NORTH);
            
            JTable table = new JTable(statistic.getValue());
            table.setShowGrid(true);
            panel.add(table, BorderLayout.CENTER);
            
            content.add(panel);
        }
        
        pack();
    }
}
