/*
 * Documentation on JBox2D can be found at:
 * http://trentcoder.github.io/JBox2D_JavaDoc/apidocs/
 */
package nl.rug.dmas.trafficdemo;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JFrame;
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
    static public Scenario readScenarioFromFile(File file) {
        // Get me a map of the world!
        StreetGraph streetGraph = GraphReader.read(file);
        
        // Create a scenario with two cars looping left and right (and colliiiddiiingg >:D )
        Scenario scenario = new Scenario(streetGraph);
        scenario.commonKnowledge.put("mouse", new Vec2(0, 0));
        scenario.commonKnowledge.put("path", new CopyOnWriteArrayList<Vec2>());
        
        return scenario;
    }
    
    static public void runFile(File file) {
        Scenario scenario = readScenarioFromFile(file);
        
        // Pony up a simple window, our only entrypoint to the app
        TrafficWindow window = new TrafficWindow(scenario);
        window.setSize(800, 600);
        window.setTitle(file.getName());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
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
        
        runFile(new File("input/graaf.txt"));
    }
}
