/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Color;
import java.util.Random;
import org.jbox2d.common.Vec2;

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
    
    static public Vec2 nextRandomVec(float min_x, float max_x, float min_y, float max_y) {
        float x = random.nextFloat() * (max_x - min_x) + min_x;
        float y = random.nextFloat() * (max_y - min_y) + min_y;
        return new Vec2(x, y);
    }
}
