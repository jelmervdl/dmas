/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jbox2d.common.MathUtils;

/**
 *
 * @author jelmer
 */
public abstract class Parameter implements Iterable<Float> {
    abstract public float getValue(Random rng);
    
    abstract public float getMax();
    
    final static private Pattern pattern = Pattern.compile("^"
        + "(?<fixed>(?<fixedval>[+-]?\\d*\\.?\\d*))"
        + "|(?<range>(?<rangelhs>[+-]?\\d*\\.?\\d*)\\-(?<rangerhs>[+-]?\\d*\\.?\\d*))"
        + "|(?<sequence>(?<seqlhs>[+-]?\\d*\\.?\\d*)\\:(?<seqstep>[+-]?\\d*\\.?\\d*)\\:(?<seqrhs>[+-]?\\d*\\.?\\d*))"
        + "$");
    
    static public Parameter fromString(String parameter) {
        Matcher matcher = pattern.matcher(parameter);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Parameter cannot be parsed");
        } else if (matcher.group("fixed") != null) {
            return new Fixed(Float.parseFloat(matcher.group("fixedval")));
        } else if (matcher.group("range") != null) {
            return new Range(
                Float.parseFloat(matcher.group("rangelhs")),
                Float.parseFloat(matcher.group("rangerhs")));
        } else if (matcher.group("sequence") != null) {
            return new Sequence(
                Float.parseFloat(matcher.group("seqlhs")),
                Float.parseFloat(matcher.group("seqrhs")),
                Float.parseFloat(matcher.group("seqstep")));
        } else {
            throw new IllegalStateException("Could not idenitfy matched pattern");
        }
    }
    
    static public class Range extends Parameter {
        final private float lower;
        final private float upper;
        
        public Range(float lower, float upper) {
            this.lower = lower;
            this.upper = upper;
        }
        
        @Override
        public float getValue(Random rng) {
            // 4 - 12
            // mean: 4 + 4 = 8
            // sd: 3/4 * (upper - lower)
            float mean = lower + (upper - lower) / 2f;
            float sd = (upper - lower) / 3f;
            return mean + MathUtils.clamp((float) rng.nextGaussian(), -3f, 3f) * sd;
        }

        @Override
        public float getMax() {
            return upper;
        }
        
        @Override
        public String toString() {
            return String.format("%s-%s", lower, upper);
        }

        @Override
        public Iterator<Float> iterator() {
            throw new UnsupportedOperationException("Not supported yet due to infinite possibilties.");
        }
    }
    
    static public class Fixed extends Parameter {
        final private float value;
        
        public Fixed(float value) {
            this.value = value;
        }
        
        @Override
        public float getValue(Random rng) {
            return value;
        }

        @Override
        public float getMax() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s", value);
        }

        @Override
        public Iterator<Float> iterator() {
            return new FixedIterator(this);
        }
    }
    
    static public class FixedIterator implements Iterator<Float> {
        final private Fixed parameter;
        
        boolean end = false;
        
        public FixedIterator(Fixed parameter) {
            this.parameter = parameter;
        }
        
        @Override
        public boolean hasNext() {
            return !end;
        }

        @Override
        public Float next() {
            end = true;
            return parameter.getValue(null);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
    
    static public class Sequence extends Parameter {
        final private float lower;
        final private float upper;
        final private float stepSize;
        
        private float value;
        
        public Sequence(float lower, float upper, float stepSize) {
            this.lower = lower;
            this.upper = upper;
            this.stepSize = stepSize;
            this.value = lower - stepSize;
        }
        
        @Override
        public float getValue(Random rng) {
            value = value + stepSize;
            
            if (value > upper)
                value = lower;
            
            return value;
        }

        @Override
        public float getMax() {
            return upper;
        }

        @Override
        public String toString() {
            return String.format("%s:%s:%s", lower, stepSize, upper);
        }

        @Override
        public Iterator<Float> iterator() {
            return new SequenceIterator(this);
        }
    }
    
    static public class SequenceIterator implements Iterator<Float> {
        final private Sequence parameter;
        
        public SequenceIterator(Sequence parameter) {
            this.parameter = parameter;
        }
        
        @Override
        public boolean hasNext() {
            return parameter.value + parameter.stepSize <= parameter.upper;
        }

        @Override
        public Float next() {
            return parameter.getValue(null);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}