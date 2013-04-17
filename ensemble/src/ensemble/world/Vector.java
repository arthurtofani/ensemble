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

package ensemble.world;

import java.text.DecimalFormat;
import java.text.NumberFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class Vector.
 */
public class Vector {

	/** The dimensions. */
	public int dimensions;
	
	/** The values. */
	private double[] values;
	
	/** The magnitude. */
	public double magnitude = 0.0;

	/** The nf. */
	static NumberFormat nf = new DecimalFormat("#0.0000");
	
//	public static final Vector MAXIMUM_VECTOR = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
//	public static final Vector ZERO_VECTOR = new Vector3D();
	
	/**
 * Instantiates a new vector.
 */
public Vector() {
		dimensions = 3;
		values = new double[dimensions];
	}
	
	/**
	 * Instantiates a new vector.
	 *
	 * @param dimensions the dimensions
	 */
	public Vector(int dimensions) {
		this.dimensions = dimensions;
		values = new double[dimensions];
	}
	
	/**
	 * Instantiates a new vector.
	 *
	 * @param ds the ds
	 */
	public Vector(double ... ds) {
		dimensions = ds.length;
		values = new double[dimensions];
		update(ds);
	}
	
	/**
	 * Sets the value.
	 *
	 * @param n the n
	 * @param value the value
	 */
	public void setValue(int n, double value) {
		this.values[n] = value;
	}

	/**
	 * Gets the value.
	 *
	 * @param n the n
	 * @return the value
	 */
	public double getValue(int n) {
		return values[n];
	}

	/**
	 * Parses the.
	 *
	 * @param str the str
	 * @return the vector
	 */
	public static Vector parse(String str) {
		Vector vec = new Vector();
		if (str.startsWith("(") && str.endsWith(")")) {
			String str2[] = str.substring(1, str.length()-1).split("[; ]");
			for (int i = 0; i < str2.length; i++) {
				try {
					vec.values[i] = Double.valueOf(str2[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			vec.updateMagnitude();
		}
		return vec;
	}
	
	
	
	/**
	 * Parses the single.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the vector
	 */
	public static Vector parseSingle(double x, double y, double z) {
		Vector vec = new Vector();
		vec.values[0] = x;
		vec.values[1] = y;
		vec.values[2] = z;

		vec.updateMagnitude();

		return vec;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0; i < values.length; i++) {
			buf.append(values[i]);
			if (i < values.length-1) {
				buf.append(";");
			}
		}
		buf.append(")");
		return (buf.toString());
	}
	
	/**
	 * To string space.
	 *
	 * @return the string
	 */
	public String toStringSpace() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			buf.append(values[i]);
			if (i < values.length-1) {
				buf.append(" ");
			}
		}
		return (buf.toString());
	}
	
	/**
	 * Copy.
	 *
	 * @param copy the copy
	 */
	public void copy(Vector copy) {
		if (this.dimensions == copy.dimensions) {
			for (int i = 0; i < values.length; i++) {
				copy.setValue(i, this.values[i]);
			}
			copy.magnitude = this.magnitude;
		}
	}
	
	/**
	 * Copy.
	 *
	 * @return the vector
	 */
	public Vector copy() {
		Vector ret = new Vector();
		if (this.dimensions == ret.dimensions) {
			for (int i = 0; i < values.length; i++) {
				ret.setValue(i, this.values[i]);
			}
			ret.magnitude = this.magnitude;
		}
		return ret;
	}

	/**
	 * Update.
	 *
	 * @param ds the ds
	 */
	public void update(double ... ds) {
		for (int i = 0; i < ds.length; i++) {
			values[i] = ds[i];
		}
		updateMagnitude();
	}
	
	/**
	 * Update magnitude.
	 */
	public void updateMagnitude() {
		double sum = 0.0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i] * values[i];
		}
		this.magnitude = Math.sqrt(sum);
	}
	
	/**
	 * Adds the.
	 *
	 * @param v the v
	 */
	public void add(Vector v) {
		if (this.dimensions == v.dimensions) {
			for (int i = 0; i < values.length; i++) {
				values[i] += v.values[i];
			}
		}
		updateMagnitude();
	}
	
	/**
	 * Adds the.
	 *
	 * @param v the v
	 * @param scale the scale
	 */
	public void add(Vector v, double scale) {
		if (this.dimensions == v.dimensions) {
			for (int i = 0; i < values.length; i++) {
				values[i] += (v.values[i] * scale);
			}
		}
		updateMagnitude();
	}
	
	/**
	 * Subtract.
	 *
	 * @param v the v
	 */
	public void subtract(Vector v) {
		if (this.dimensions == v.dimensions) {
			for (int i = 0; i < values.length; i++) {
				values[i] -= v.values[i];
			}
		}
		updateMagnitude();
	}
	
	/**
	 * Subtract.
	 *
	 * @param v the v
	 * @param scale the scale
	 */
	public void subtract(Vector v, double scale) {
		if (this.dimensions == v.dimensions) {
			for (int i = 0; i < values.length; i++) {
				values[i] -= (v.values[i] * scale);
			}
		}
		updateMagnitude();
	}
	
	/**
	 * Gets the magnitude.
	 *
	 * @return the magnitude
	 */
	public double getMagnitude() {
		return magnitude;
	}
	
	/**
	 * Dot product.
	 *
	 * @param v the v
	 * @return the double
	 */
	public double dotProduct(Vector v) {
		double res = 0.0;
		if (this.dimensions == v.dimensions) {
			for (int i = 0; i < values.length; i++) {
				res += this.values[i] * v.values[i];
			}
		}
		return res;
	}
	
	/**
	 * Product.
	 *
	 * @param value the value
	 */
	public void product(double value) {
		for (int i = 0; i < values.length; i++) {
			values[i] *= value;
		}
		updateMagnitude();
	}
	
	/**
	 * Division.
	 *
	 * @param value the value
	 */
	public void division(double value) {
		for (int i = 0; i < values.length; i++) {
			values[i] /= value;
		}
		updateMagnitude();
	}
	
	/**
	 * Normalize vector.
	 */
	public void normalizeVector() {
		
		if (getMagnitude() != 0.0) {
			for (int i = 0; i < values.length; i++) {
				values[i] /= magnitude;
			}
			updateMagnitude();
		}
		
	}
	
	/**
	 * Inverse.
	 */
	public void inverse() {
		for (int i = 0; i < values.length; i++) {
			values[i] = -values[i];
		}
	}
	
	/**
	 * Normalize vector inverse.
	 */
	public void normalizeVectorInverse() {
		
		normalizeVector();
		for (int i = 0; i < values.length; i++) {
			values[i] = -values[i];
		}
		
	}
	
    /**
     * Gets the distance.
     *
     * @param otherVector the other vector
     * @return the distance
     */
    public double getDistance(Vector otherVector) {
    	
    	double distance = 0.0;
    	for (int i = 0; i < values.length; i++) {
    		double aux = (values[i] - otherVector.values[i]); 
    		distance += aux * aux;
		}
    	return Math.sqrt(distance);
    	
    }
    
    /**
     * Zero.
     */
    public void zero() {

    	for (int i = 0; i < values.length; i++) {
    		values[i] = 0.0;
		}
    	magnitude = 0.0;
    	
    }
	
}
