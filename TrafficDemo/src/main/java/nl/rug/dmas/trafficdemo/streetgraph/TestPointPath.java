/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.streetgraph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nl.rug.dmas.trafficdemo.VecUtils;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class TestPointPath extends JPanel {
    @Override
    public void paint(Graphics g) {
        paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setColor(Color.RED);
        Vec2 a = new Vec2(30, 250);
        Vec2 b = new Vec2(100, 100);
        Vec2 c = new Vec2(200, 100);
        
        
        g2.draw(getLine(a, b));
        g2.draw(getLine(b, c));
        
        Vec2 center = VecUtils.getCenter(a, b);
        Vec2 normal = VecUtils.getNormal(a, b);
        
        g2.setColor(Color.GREEN);
        g2.draw(getLine(center, center.add(normal.mul(30))));
        
        Vec2 vertexNormal = VecUtils.getNormal(a, b, c);
        g2.setColor(Color.BLUE);
        g2.draw(getLine(b, b.add(vertexNormal.mul(30))));
        
        g2.dispose();
    }
    
    private Line2D.Float getLine(Vec2 a, Vec2 b) {
        return new Line2D.Float(a.x, a.y, b.x, b.y);
    }
    
    static public void main(String[] args) {
        JFrame window = new JFrame();
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TestPointPath panel = new TestPointPath();
        window.getContentPane().add(panel);
        
        window.setVisible(true);
        window.repaint();
    }
}
