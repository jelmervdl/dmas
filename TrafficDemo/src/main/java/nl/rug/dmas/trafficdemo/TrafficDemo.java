/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.jbox2d.testbed.framework.TestList;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedErrorHandler;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.j2d.DebugDrawJ2D;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.jbox2d.testbed.framework.j2d.TestbedSidePanel;

/**
 *
 * @author jelmer
 */
public class TrafficDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestbedModel model = new TestbedModel();
        final TestbedController controller = new TestbedController(model,
                TestbedController.UpdateBehavior.UPDATE_CALLED,
                TestbedController.MouseBehavior.NORMAL,
                new TestbedErrorHandler() {
                    @Override
                    public void serializationError(Exception e, String message) {
                      JOptionPane.showMessageDialog(null, message, "Serialization Error",
                          JOptionPane.ERROR_MESSAGE);
                    }
                  });
        
        TestPanelJ2D panel = new TestPanelJ2D(model, controller);
        model.setPanel(panel);
        model.setDebugDraw(new DebugDrawJ2D(panel, true));
        
        model.addTest(new CarTest());
        TestList.populateModel(model);
        
        JFrame testbed = new JFrame();
        testbed.setTitle("JBox2D Testbed");
        testbed.setLayout(new BorderLayout());
        TestbedSidePanel side = new TestbedSidePanel(model, controller);
        testbed.add((Component) panel, "Center");
        testbed.add(new JScrollPane(side), "East");
        testbed.pack();
        testbed.setVisible(true);
        testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.playTest(0);
                controller.start();
            }  
        });
    }
}
