/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTable;

/**
 *
 * @author jelmer
 */
public class StatisticsWindow extends JFrame {
    final ScenarioStatistics statistics;
    
    public StatisticsWindow(ScenarioStatistics statistics) {
        this.statistics = statistics;
        
        setPreferredSize(new Dimension(300, 300));
        setTitle("Statistics");
        setType(Type.UTILITY);
        
        JTable table = new JTable(statistics);
        add(table);
        
        pack();
    }
}
