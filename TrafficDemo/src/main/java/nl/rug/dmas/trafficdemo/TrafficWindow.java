/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.BorderLayout;
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
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class TrafficWindow extends JFrame {
    private final Scenario scenario;
    
    private final TrafficPanel panel;
    
    private final StatisticsWindow statisticsWindow;
    
    public TrafficWindow(Scenario scenario) {
        this.scenario = scenario;
        
        setTitle("Traffic!");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        statisticsWindow = new StatisticsWindow(scenario);
        
        // The TrafficPanel draws the actual scenario (cars etc.)
        panel = new TrafficPanel(scenario);
        panel.drawFOV = TrafficDemo.getPreferences().getBoolean("drawFOV", panel.drawFOV); // prefer user stored preference
        panel.drawDirection = TrafficDemo.getPreferences().getBoolean("drawDirection", panel.drawDirection);
        add(panel);
        
        // Update the panel when the scenario changes
        scenario.addListener(panel);
        
        // Allow us to draw on the canvas to create a path for the steerAlongPath behavior of drivers.
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                CopyOnWriteArrayList<Vec2> path = (CopyOnWriteArrayList<Vec2>) TrafficWindow.this.scenario.getCommonKnowledge().get("path");
                path.add(panel.getMouseWorldLocation());
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                TrafficWindow.this.scenario.getCommonKnowledge().put("mouse", panel.getMouseWorldLocation());
            }
        });
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    triggerPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    triggerPopup(e);
            }
            
            private void triggerPopup(final MouseEvent popupEvent) {
                JPopupMenu carContextMenu = new JPopupMenu();
                final Car car = panel.getCarAtPosition(popupEvent.getPoint());
                
                // Pause the scenario while the context menu is open
                carContextMenu.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        try {
                            TrafficWindow.this.scenario.stop();
                        } catch (InterruptedException error) {
                            JOptionPane.showMessageDialog(TrafficWindow.this, error.getMessage());
                        }
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        TrafficWindow.this.scenario.start();
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                        //
                    }
                });
                
                // Menu item for removing the car (if one was selected)
                carContextMenu.add(new AbstractAction("Remove Car") {
                    @Override
                    public boolean isEnabled() {
                        return car != null;
                    }
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TrafficWindow.this.scenario.remove(car);
                    }
                });
                
                // Menu item for spawning a car at this position
                carContextMenu.add(new AbstractAction("Add Car") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vec2 position = panel.getPositionInWorld(popupEvent.getPoint());
                        TrafficWindow.this.scenario.add(generateCar(position));
                    }
                });
                
                popupEvent.consume();
                carContextMenu.show(panel, popupEvent.getX(), popupEvent.getY());
            }
        });
        
        // Always stop the scenario when this window is closed.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    TrafficWindow.this.scenario.stop();
                } catch (InterruptedException error) {
                    // Do nothing, it will propably quit someday automatically
                }
            }
        });
        
        // Add a menu bar for some configuration toggles
        initMenuBar();
    }
    
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        final JMenuItem openScenario = new JMenuItem("Open…");
        fileMenu.add(openScenario);
        openScenario.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openScenario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TrafficDemo.openFile();
            }
        });
        
        fileMenu.addSeparator();
        
        final JMenuItem closeWindow = new JMenuItem("Close Window");
        fileMenu.add(closeWindow);
        closeWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        closeWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TrafficWindow.this.dispose();
            }
        });
                
        
        JMenu simulationMenu = new JMenu("Simulation");
        menuBar.add(simulationMenu);
        
        final JCheckBoxMenuItem runSimulation = new JCheckBoxMenuItem("Run", true);
        simulationMenu.add(runSimulation);
        runSimulation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        runSimulation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (runSimulation.isSelected())
                        scenario.start();
                    else
                        scenario.stop();
                } catch (InterruptedException error) {
                    JOptionPane.showMessageDialog(
                        TrafficWindow.this, error.getMessage(),
                        "Could not stop the simulation",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Update the "Run" checkbox with the actual state of the simulation
        scenario.addListener(new ScenarioAdapter() {
            @Override
            public void scenarioStarted() {
                runSimulation.setSelected(true);
            }

            @Override
            public void scenarioStopped() {
                runSimulation.setSelected(false);
            }
        });
        
        final JMenuItem jumpSimulation = new JMenuItem("Jump to…");
        simulationMenu.add(jumpSimulation);
        jumpSimulation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        jumpSimulation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String timeString = JOptionPane.showInputDialog(TrafficWindow.this,
                        "Jump to time (in seconds):",
                        String.format("%.2f", scenario.getTime()));
                
                // If we cancelled the dialog, do nothing
                if (timeString == null)
                    return;
                
                final float time = Float.parseFloat(timeString);
                
                final JDialog progressDialog = new JDialog(TrafficWindow.this, "Jumping…", JDialog.ModalityType.DOCUMENT_MODAL);
                progressDialog.setSize(300, 75);
                progressDialog.setLocationRelativeTo(TrafficWindow.this);
                
                JPanel progressPanel = (JPanel) progressDialog.getContentPane();
                progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                final JLabel progressLabel = new JLabel(String.format("Jumping to %s", TimeUtil.formatTime(time)));
                progressDialog.getContentPane().add(progressLabel, BorderLayout.NORTH);

                final JProgressBar progressBar = new JProgressBar((int) scenario.getTime(), (int) time);
                progressDialog.getContentPane().add(progressBar, BorderLayout.CENTER);
                
                // We create a separate scenario listener that updates the progress bar
                final ScenarioListener progressListener = new ScenarioAdapter() {
                    @Override
                    public void scenarioStepped() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setValue((int) scenario.getTime());
                            }
                        });
                    }
                };

                // This worker switches out the listeners and performs the 
                // actual (blocking) jump. We need to do this in a worker the
                // thread we are running in will be blocked on the modal's
                // JDialog.setVisible(true).
                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        scenario.stop();

                        try {
                            scenario.removeListener(panel);
                            scenario.addListener(progressListener);

                            scenario.jumpTo(time);
                        } catch (InterruptedException error) {
                            // shit
                        } finally {
                            scenario.addListener(panel);
                            scenario.removeListener(progressListener);
                        }

                        return null;
                    }
                    
                    // Once done, hide the progress dialog and paint the
                    // current scenario. No need for SwingUtilities.invokeLater
                    // this method will be automatically executed in the right
                    // thread for UI updates
                    @Override
                    public void done() {
                        progressDialog.setVisible(false);
                        panel.repaint();
                    }
                };
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setVisible(true);
                    }
                });

                worker.execute();
            }
        });
        
        simulationMenu.addSeparator();
        
        final JMenuItem selectCars = new JMenuItem("Select All");
        simulationMenu.add(selectCars);
        selectCars.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        selectCars.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scenario.selectedCars.addAll(scenario.cars);
            }
        });
        
        final JMenuItem addCar = new JMenuItem("Add a Car");
        simulationMenu.add(addCar);
        addCar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addCar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scenario.add(generateCar());
            }
        });
        
        final JMenuItem removeCar = new JMenuItem("Remove Cars");
        simulationMenu.add(removeCar);
        removeCar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        removeCar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Car car : new HashSet<>(scenario.selectedCars))
                    scenario.remove(car);
            }
        });
        
        // Disable the remove car action by default, but listen to the scenario
        // to check whether we have a selection to remove.
        removeCar.setEnabled(false);
        scenario.addListener(new ScenarioAdapter() {
            @Override
            public void selectionChanged() {
                removeCar.setText(String.format("Remove %s", scenario.selectedCars.size() == 1 ? "Car" : "Cars"));
                removeCar.setEnabled(scenario.selectedCars.size() > 0);
            }
        });
        
        
        simulationMenu.addSeparator();
        
        final JCheckBoxMenuItem spawnCars = new JCheckBoxMenuItem("Spawn Cars", true);
        simulationMenu.add(spawnCars);
        spawnCars.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                scenario.getCommonKnowledge().put("spawnCars", spawnCars.isSelected());
            }
        });
        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        
        final JMenuItem showStatistics = new JMenuItem("Statistics");
        viewMenu.add(showStatistics);
        showStatistics.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        showStatistics.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statisticsWindow.setVisible(true);
                statisticsWindow.toFront();
            }
        });
        
        viewMenu.addSeparator();
        
        final JCheckBoxMenuItem drawFOV = new JCheckBoxMenuItem("Show Field of View", panel.drawFOV);
        viewMenu.add(drawFOV);
        drawFOV.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawFOV = drawFOV.isSelected();
                TrafficDemo.getPreferences().putBoolean("drawFOV", panel.drawFOV);
            }
        });
        
        final JCheckBoxMenuItem drawDirection = new JCheckBoxMenuItem("Show Direction", panel.drawDirection);
        viewMenu.add(drawDirection);
        drawDirection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawDirection = drawDirection.isSelected();
                TrafficDemo.getPreferences().putBoolean("drawDirection", panel.drawDirection);
            }
        });
        
        final JCheckBoxMenuItem drawDriverThoughts = new JCheckBoxMenuItem("Show Driver Thoughts", panel.drawDriverThoughts);
        viewMenu.add(drawDriverThoughts);
        drawDriverThoughts.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawDriverThoughts = drawDriverThoughts.isSelected();
                TrafficDemo.getPreferences().putBoolean("drawDriverThoughts", panel.drawDriverThoughts);
            }
        });
    }
    
    private Car generateCar() {
        return generateCar(RandomUtil.nextRandomVec(-10, 10, -10, 10));
    }
    
    private Car generateCar(Vec2 position) {
        return new Car(scenario.createDriver(), 2, 4, position);
    }
}
