/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Color;
import java.util.Random;

/**
 *
 * @author jelmer
 */
public class RandomUtil {
    private static final Random random = new Random();
    
    static public Color nextRandomPastelColor() {
        final float hue = random.nextFloat();
        // Saturation between 0.1 and 0.3
        final float saturation = (random.nextInt(2000) + 1000) / 10000f;
        final float luminance = 0.9f;
        return Color.getHSBColor(hue, saturation, luminance);
    }
}
