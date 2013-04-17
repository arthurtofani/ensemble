/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.clock;

// TODO: Auto-generated Javadoc
/**
 * The Enum TimeUnit.
 */
public enum TimeUnit {

    /** The nanoseconds. */
    NANOSECONDS {
        public double toNanos(double d)   { return d; }
        public double toMicros(double d)  { return d/(C1/C0); }
        public double toMillis(double d)  { return d/(C2/C0); }
        public double toSeconds(double d) { return d/(C3/C0); }
        public double convert(double d, TimeUnit u) { return u.toNanos(d); }
    },
    
    /** The microseconds. */
    MICROSECONDS {
        public double toNanos(double d)   { return x(d, C1/C0, MAX/(C1/C0)); }
        public double toMicros(double d)  { return d; }
        public double toMillis(double d)  { return d/(C2/C1); }
        public double toSeconds(double d) { return d/(C3/C1); }
        public double convert(double d, TimeUnit u) { return u.toMicros(d); }
    },
    
    /** The milliseconds. */
    MILLISECONDS {
        public double toNanos(double d)   { return x(d, C2/C0, MAX/(C2/C0)); }
        public double toMicros(double d)  { return x(d, C2/C1, MAX/(C2/C1)); }
        public double toMillis(double d)  { return d; }
        public double toSeconds(double d) { return d/(C3/C2); }
        public double convert(double d, TimeUnit u) { return u.toMillis(d); }
    },
    
    /** The seconds. */
    SECONDS {
        public double toNanos(double d)   { return x(d, C3/C0, MAX/(C3/C0)); }
        public double toMicros(double d)  { return x(d, C3/C1, MAX/(C3/C1)); }
        public double toMillis(double d)  { return x(d, C3/C2, MAX/(C3/C2)); }
        public double toSeconds(double d) { return d; }
        public double convert(double d, TimeUnit u) { return u.toSeconds(d); }
    },
    
    /** The samples. */
    SAMPLES {
    	
    },
    
    /** The events. */
    EVENTS {
    	
    },
    
    /** The turns. */
    TURNS {
    	
    };
	
    // Handy constants for conversion methods
    /** The Constant C0. */
    static final double C0 = 1;
    
    /** The Constant C1. */
    static final double C1 = C0 * 1000;
    
    /** The Constant C2. */
    static final double C2 = C1 * 1000;
    
    /** The Constant C3. */
    static final double C3 = C2 * 1000;

    /** The Constant MAX. */
    static final double MAX = Double.MAX_VALUE;
	
    /**
     * Scale d by m, checking for overflow.
     * This has a short name to make above code more readable.
     *
     * @param d the d
     * @param m the m
     * @param over the over
     * @return the double
     */
    static double x(double d, double m, double over) {
        if (d >  over) return Double.MAX_VALUE;
        if (d < -over) return Double.MIN_VALUE;
        return d * m;
    }

    /**
     * Convert.
     *
     * @param sourceDuration the source duration
     * @param sourceUnit the source unit
     * @return the double
     */
    public double convert(double sourceDuration, TimeUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    /**
     * To nanos.
     *
     * @param duration the duration
     * @return the double
     */
    public double toNanos(double duration) {
        throw new AbstractMethodError();
    }

    /**
     * To micros.
     *
     * @param duration the duration
     * @return the double
     */
    public double toMicros(double duration) {
        throw new AbstractMethodError();
    }

    /**
     * To millis.
     *
     * @param duration the duration
     * @return the double
     */
    public double toMillis(double duration) {
        throw new AbstractMethodError();
    }

    /**
     * To seconds.
     *
     * @param duration the duration
     * @return the double
     */
    public double toSeconds(double duration) {
        throw new AbstractMethodError();
    }

}