/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import org.jbox2d.dynamics.World;

/**
 *
 * @author jelmer
 */
public class Scenario {
    World world;
    
    ArrayList<Car> cars = new ArrayList<>();
    
    public Scenario(World world) {
        this.world = world;
    }
    
    public void step(float dt) {
        for (Car car : cars)
            car.update(dt);
    }
}
