/*
 * Documentation on JBox2D can be found at:
 * http://trentcoder.github.io/JBox2D_JavaDoc/apidocs/
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
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
    
    static public Scenario readScenarioFromFile(File file) {
        // Get me a map of the world!
        StreetGraph streetGraph = GraphReader.read(file);
        
        // Create a scenario with two cars looping left and right (and colliiiddiiingg >:D )
        Scenario scenario = new Scenario(streetGraph);
        scenario.commonKnowledge.put("mouse", new Vec2(0, 0));
        scenario.commonKnowledge.put("path", new CopyOnWriteArrayList<Vec2>());
        
        return scenario;
    }
    
    static public void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(getLastOpenedFile());

        switch (fileChooser.showOpenDialog(null)) {
            case JFileChooser.APPROVE_OPTION:
                prefs.put("lastOpenedFile", fileChooser.getSelectedFile().getPath());
                runFile(fileChooser.getSelectedFile());
                break;
        }
    }
    
    static public void runFile(File file) {
        Scenario scenario = readScenarioFromFile(file);
        
        // Pony up a simple window, our only entrypoint to the app
        TrafficWindow window = new TrafficWindow(scenario);
        window.setSize(800, 600);
        window.setTitle(file.getName());
        
        windows.add(window);
        
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("Window closed");
                windows.remove(e.getWindow());
                
                if (windows.isEmpty())
                    System.exit(0);
            }
        });
        
        // Show the window, and start the loop!
        window.setVisible(true);
        scenario.start();
    }

    static public void main(String[] args) {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Could not initialize awesome Mac-specific UI, but Java UI will be sort of fine, I guess.
        }
        
        if (args.length > 1) {
            for (int i = 1; i < args.length; ++i)
                runFile(new File(args[i]));
        }
        else {
            File file = getLastOpenedFile();
            
            if (file == null || !file.exists())
                file = new File("input/graaf.txt");
            
            runFile(file);
        }
    }
}
