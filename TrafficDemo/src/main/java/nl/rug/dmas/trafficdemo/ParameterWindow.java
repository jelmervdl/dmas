/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author jelmer
 */
public class ParameterWindow extends JFrame {
    final Scenario scenario;
    
    final List<ParameterField> fields = new ArrayList<>();
    
    public ParameterWindow(final Scenario scenario) {
        this.scenario = scenario;
        
        setTitle("Scenario settings");
        setType(Window.Type.UTILITY);
        
        SpringLayout layout = new SpringLayout();
        
        Container content = getContentPane();
        content.setLayout(layout);
        
        fields.add(new ParameterField("Car Width") {
            @Override
            public String getValue() {
                return scenario.carWidth.toString();
            }

            @Override
            public void setValue(String value) {
                scenario.carWidth = Parameter.fromString(value);
            }
        });
        
        fields.add(new ParameterField("Car Length") {
            @Override
            public String getValue() {
                return scenario.carLength.toString();
            }
            
            @Override
            public void setValue(String value) {
                scenario.carLength = Parameter.fromString(value);
            }
        });
        
        fields.add(new ParameterField("Autonomous Car Ratio") {
            @Override
            public String getValue() {
                return scenario.ratioAutonomousCars.toString();
            }

            @Override
            public void setValue(String value) {
                scenario.ratioAutonomousCars = Parameter.fromString(value);
            }
        });
        
        fields.add(new ParameterField("View Length Human Drivers") {
            @Override
            public String getValue() {
                return scenario.viewLength.toString();
            }

            @Override
            public void setValue(String value) {
                scenario.viewLength = Parameter.fromString(value);
            }
        });
        
        for (ParameterField field : fields) {
            JLabel label = new JLabel(field.getLabel(), JLabel.TRAILING);
            JTextField textField = new JTextField(field.getValue(), 15);
            textField.addActionListener(field);
            label.setLabelFor(textField);
            
            content.add(label);
            content.add(textField);
        }
        
        SpringUtilities.makeCompactGrid(content,
                fields.size(), 2, // rows, cols
                6, 6, // initX, initY
                6, 6  // xPad, yPad
        );
        
        pack();
    }
    
    static abstract class ParameterField implements ActionListener {
        String label;
        
        public ParameterField(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return this.label;
        }
        
        abstract public String getValue();
        
        abstract public void setValue(String value);

        @Override
        public void actionPerformed(ActionEvent e) {
            setValue(((JTextField) e.getSource()).getText());
        }
    }
}
