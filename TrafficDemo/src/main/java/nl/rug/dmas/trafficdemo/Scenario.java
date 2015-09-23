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
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

/**
 *
 * @author jelmer
 */
public class Scenario {
    World world;
    
    /**
     * A list of all cars in the simulation.
     */
    ArrayList<Car> cars = new ArrayList<>();
    
    /**
     * A map of locations known to all agents (such as the mouse ;) )
     */
    Map<String, Object> commonKnowledge = new HashMap<>();
    
    public Scenario(World world) {
        this.world = world;
        
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
    
    public void step(float dt) {
        for (Car car : cars) {
            car.driver.step();
            car.update(dt);
        }
    }
}
