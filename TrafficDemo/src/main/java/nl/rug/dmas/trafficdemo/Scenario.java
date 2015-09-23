/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * A scenario, which is simulated in a world and updates the state of the
 * cars and drivers from time step to time step.
 * @author jelmer
 */
public class Scenario {
    World world;
    
    // A list of all cars in the simulation.
    ArrayList<Car> cars = new ArrayList<>();
    
    // A map of locations known to all agents (such as the mouse ;) )
    Map<String, Object> commonKnowledge = new HashMap<>();
    
    /**
     * A scenario takes an instance of a JBox2D world and sets the contact
     * listener. This listener updates the fixturesInSight list of the drivers
     * throughout the simulation.
     */
    public Scenario() {
        // Create a world without gravity (2d world seen from top, eh!)
        // The world is our physics simulation.
        world = new World(new Vec2(0, 0));

        // Keep a contact listener that monitors whether cars are in sight of
        // drivers.
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact fixtureContact) {
                DriverContact contact = new DriverContact(fixtureContact);
                
                if (contact.driver != null && contact.driver.car != contact.fixture.getUserData())
                    contact.driver.fixturesInSight.add(contact.fixture);
            }

            @Override
            public void endContact(Contact fixtureContact) {
                DriverContact contact = new DriverContact(fixtureContact);
                
                if (contact.driver != null && contact.driver.car != contact.fixture.getUserData())
                    contact.driver.fixturesInSight.remove(contact.fixture);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                //
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
               //
            }
            
        });
    }
    
    /**
     * Steps the simulation of dt seconds.
     * @param dt delta time in seconds
     */
    public void step(float dt) {
        for (Car car : cars) {
            car.driver.step();
            car.update(dt);
        }
        
        world.step(dt, 3, 8);
    }
}
