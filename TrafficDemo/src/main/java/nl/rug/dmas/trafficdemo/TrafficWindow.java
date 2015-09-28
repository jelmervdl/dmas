/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class TrafficWindow extends JFrame {
    private final Preferences prefs = Preferences.userNodeForPackage(TrafficDemo.class);
    
    private final Scenario scenario;
    
    private final TrafficPanel panel;
    
    public TrafficWindow(Scenario scenario) {
        this.scenario = scenario;
        
        setTitle("Traffic!");
        
        // The TrafficPanel draws the actual scenario (cars etc.)
        panel = new TrafficPanel(scenario);
        panel.drawFOV = prefs.getBoolean("drawFOV", panel.drawFOV); // prefer user stored preference
        panel.drawDirection = prefs.getBoolean("drawDirection", panel.drawDirection);
        add(panel);
        
        // Allow us to draw on the canvas to create a path for the steerAlongPath behavior of drivers.
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                CopyOnWriteArrayList<Vec2> path = (CopyOnWriteArrayList<Vec2>) TrafficWindow.this.scenario.commonKnowledge.get("path");
                path.add(panel.getMouseWorldLocation());
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                TrafficWindow.this.scenario.commonKnowledge.put("mouse", panel.getMouseWorldLocation());
            }
        });
        
        // Also, when the scenario updates, we redraw.
        scenario.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                repaint();
            }
        });
        
        // Always stop the scenario when this window is closed.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                TrafficWindow.this.scenario.stop();
            }
        });
        
        // Add a menu bar for some configuration toggles
        initMenuBar();
    }
    
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu simulationMenu = new JMenu("Simulation");
        menuBar.add(simulationMenu);
        
        final JMenuItem addCar = new JMenuItem("Add a car");
        simulationMenu.add(addCar);
        addCar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addCar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scenario.add(generateCar());
            }
        });
        
        final JMenuItem removeCar = new JMenuItem("Remove a car");
        simulationMenu.add(removeCar);
        removeCar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        removeCar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random generator = new Random();
                if (scenario.cars.size() > 0) {
                    Car car = scenario.cars.get(generator.nextInt(scenario.cars.size()));
                    scenario.remove(car);
                }
            }
        });
        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        
        final JCheckBoxMenuItem drawFOV = new JCheckBoxMenuItem("Show Field of View", panel.drawFOV);
        viewMenu.add(drawFOV);
        drawFOV.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawFOV = drawFOV.isSelected();
                prefs.putBoolean("drawFOV", panel.drawFOV);
            }
        });
        
        final JCheckBoxMenuItem drawDirection = new JCheckBoxMenuItem("Show Direction", panel.drawDirection);
        viewMenu.add(drawDirection);
        drawDirection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawDirection = drawDirection.isSelected();
                prefs.putBoolean("drawDirection", panel.drawDirection);
            }
        });
    }
    
    private Car generateCar() {
        return new Car(new Driver(scenario), 2, 4, RandomUtil.nextRandomVec(-10, 10, -10, 10));
    }
}
