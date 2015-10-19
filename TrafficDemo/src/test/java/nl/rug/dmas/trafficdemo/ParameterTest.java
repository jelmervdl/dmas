/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.Random;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jelmer
 */
public class ParameterTest {
    
    Random oracle;
    
    public ParameterTest() {
    }
    
    @Before
    public void setUp() {
        oracle = new Random() {
            @Override
            public float nextFloat() {
                return 1.5f;
            }

            @Override
            public synchronized double nextGaussian() {
                return 0.0f;
            }
        };
    }

    /**
     * Test of getValue method, of class Parameter.
     */
    @Test
    public void testFixedGetValue() {
        Parameter parameter = new Parameter.Fixed(1.0f);
        float result = parameter.getValue(oracle);
        float expResult = 1.0f;
        
        assertEquals(expResult, result, 0.0);
    }
    
    /**
     *
     */
    @Test
    public void testRangeGetValue() {
        Parameter parameter = new Parameter.Range(1.0f, 5.0f);
        float result = parameter.getValue(oracle);
        float expResult = 3.0f;
        assertEquals(expResult, result, 0.005);
    }
    
    @Test
    public void testSequenceGetValue() {
        Parameter parameter = new Parameter.Sequence(1.0f, 1.5f, 0.2f);
        
        float result = parameter.getValue(oracle);
        float expResult = 1.0f;
        assertEquals(expResult, result, 0.005);
        
        result = parameter.getValue(oracle);
        expResult = 1.2f;
        assertEquals(expResult, result, 0.005);
        
        result = parameter.getValue(oracle);
        expResult = 1.4f;
        assertEquals(expResult, result, 0.005);
        
        result = parameter.getValue(oracle);
        expResult = 1.0f;
        assertEquals(expResult, result, 0.005);
    }
    
     @Test
    public void testSequenceNegativeStart() {
        Parameter parameter = new Parameter.Sequence(-1.0f, 1.5f, 0.2f);
        
        float result = parameter.getValue(oracle);
        float expResult = -1.0f;
        assertEquals(expResult, result, 0.005);
        
        result = parameter.getValue(oracle);
        expResult = -0.8f;
        assertEquals(expResult, result, 0.005);
    }


    /**
     * Test of fromString method, of class Parameter.
     */
    @Test
    public void testFixedFromString() {
        Parameter result = Parameter.fromString("1");
        assertTrue(result instanceof Parameter.Fixed);
        assertEquals(result.getValue(oracle), 1.0f, 0.005);
    }
    
    @Test
    public void testFixedFloatFromString() {
        Parameter result = Parameter.fromString("1.5");
        assertTrue(result instanceof Parameter.Fixed);
        assertEquals(result.getValue(oracle), 1.5f, 0.005);
    }
    
    @Test
    public void testRangeFromString() {
        Parameter result = Parameter.fromString("1-5");
        assertTrue(result instanceof Parameter.Range);
        assertEquals(result.getValue(oracle), 3.0f, 0.005);
    }
    
    @Test
    public void testRangeFloatFromString() {
        Parameter result = Parameter.fromString("1.0-5.0");
        assertTrue(result instanceof Parameter.Range);
        assertEquals(result.getValue(oracle), 3.0f, 0.005);
    }
    
    @Test
    public void testSequenceFromString() {
        Parameter result = Parameter.fromString("1:2:5");
        assertTrue(result instanceof Parameter.Sequence);
        assertEquals(result.getValue(oracle), 1.0f, 0.005);
        assertEquals(result.getValue(oracle), 3.0f, 0.005);
    }
    
    @Test
    public void testSequenceFloatFromString() {
        Parameter result = Parameter.fromString("1.0:1.5:5.5");
        assertTrue(result instanceof Parameter.Sequence);
        assertEquals(result.getValue(oracle), 1.0f, 0.005);
        assertEquals(result.getValue(oracle), 2.5f, 0.005);
    }
}
