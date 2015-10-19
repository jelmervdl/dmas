/*
 * Documentation on JBox2D can be found at:
 * http://trentcoder.github.io/JBox2D_JavaDoc/apidocs/
 */
package nl.rug.dmas.trafficdemo;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import nl.rug.dmas.trafficdemo.streetgraph.GraphReader;
import nl.rug.dmas.trafficdemo.streetgraph.StreetGraph;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class TrafficDemo {
    static private final List<Window> windows = new ArrayList<>();
    
    static private final Preferences prefs = Preferences.userNodeForPackage(TrafficDemo.class);
    
    static public Preferences getPreferences() {
        return prefs;
    }
    
    static public File getLastOpenedFile() {
        String path = prefs.get("lastOpenedFile", null);
        return path == null ? null : new File(path);
    }
    
    static public Scenario readScenarioFromFile(File file, long seed) {
        // Get me a map of the world!
        StreetGraph streetGraph = GraphReader.read(file);
        
        Scenario scenario = new Scenario(streetGraph, seed);
        
        // Add a bit of common knowledge used for debugging to the scenario
        scenario.commonKnowledge.put("mouse", new Vec2(0, 0));
        scenario.commonKnowledge.put("path", new CopyOnWriteArrayList<Vec2>());

        return scenario;
    }
    
    static public void openFile() {
        openFile(System.currentTimeMillis());
    }
    
    static public void openFileWithSpecificSeed() {
        String seedString = JOptionPane.showInputDialog(null, "Seed:", "");
        if (seedString == null)
            return;
        
        long seed = Long.parseLong(seedString);
        openFile(seed);
    }

    static private void openFile(long seed) {
        FileDialog fileChooser = new FileDialog((Frame) null, "Open a graph…", FileDialog.LOAD);
        File lastOpenedFile = getLastOpenedFile();
        if (lastOpenedFile != null)
            fileChooser.setDirectory(lastOpenedFile.getParent());
        fileChooser.setVisible(true);
        for (File file : fileChooser.getFiles()) {
            prefs.put("lastOpenedFile", file.getPath());
            runFile(file, seed);
        }
    }
    
    static public void runFile(File file) {
        Scenario scenario = readScenarioFromFile(file, System.currentTimeMillis());
        runScenario(scenario, file);
    }
    
    static public void runFile(File file, long seed) {
        Scenario scenario = readScenarioFromFile(file, seed);
        runScenario(scenario, file);
    }
    
    static public void runScenario(Scenario scenario, File file) {
        // Pony up a simple window, our only entrypoint to the app
        TrafficWindow window = new TrafficWindow(scenario);
        window.setSize(800, 600);
        
        window.setTitle(file.getName());

        // On Mac OS X, show the actual graph file in the window title
        window.getRootPane().putClientProperty("Window.documentFile", file);
        
        // Keep track of the window
        windows.add(window);
        
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                windows.remove(e.getWindow());
                
                // If all windows are closed, stop the application
                if (windows.isEmpty())
                    System.exit(0);
            }
        });
        
        // Show the window, and start the loop!
        window.setLocationByPlatform(true);
        window.setVisible(true);
        window.panel.scaleToFit(window);
        scenario.start();
    }

    static public void main(String[] args) {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Could not initialize awesome Mac-specific UI, but Java UI will be sort of fine, I guess.
        }

        // if there are files passed as arguments on the commandline, open those
        if (args.length > 1) {
            for (int i = 1; i < args.length; ++i)
                runFile(new File(args[i]));
        }
        // Otherwise, try to open the last opened file
        else {
            File file = getLastOpenedFile();
            
            // No last opened file? Try the default input/graaf.txt
            if (file == null || !file.exists())
                file = new File("input/graaf.txt");
            
            // And if that isn't there, ask the user about a file
            if (file.exists())
                runFile(file);
            else
                openFile();
        }
    }
}
