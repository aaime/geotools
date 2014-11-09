/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.styling.css.util;

import java.util.BitSet;

/**
 * The signature for a set of values
 * 
 * @author Andrea Aime - GeoSolutions
 */
public abstract class Signature implements Cloneable {
	
	Object payload;
	
	public abstract void increment();
	
	public abstract boolean contains(Signature other, int k);

	public abstract boolean get(int i);
	
	public abstract void set(int idx, boolean b);
	
	public abstract int size();
	
	public abstract Object clone();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			if(get(i)) {
				sb.append("1");
			} else {
				sb.append("0");
			}
		}
		
		return sb.toString();
	}
	
	public static Signature newSignature(int size) {
		return new BitsetSignature(size);
	}

	public static class BitsetSignature extends Signature {
		BitSet bs;
		private int size;

		public BitsetSignature(int size) {
			this.size = size;
			bs = new BitSet(size);
		}
		
		public BitsetSignature(BitsetSignature other) {
			this.bs = (BitSet) other.bs.clone();
			this.size = other.size;
		}

		public void increment() {
			for(int i = 0; i < bs.size(); i++) {
				if(bs.get(i)) {
					bs.clear(i);
				} else {
					bs.set(i);
					return;
				}
			}
			
		}
		
		public boolean contains(Signature otherSignature, int k) {
			BitsetSignature other = (BitsetSignature) otherSignature;
			final int max = Math.min(bs.size(), k);
			int found = 0;
			for (int i = 0; i < max; i++) {
				boolean ob = other.bs.get(i);
				boolean b = bs.get(i);
				found += ob ? 1 : 0;
				if(ob && !b) {
					return false;
				}
			}
			
			return found > 0;
		}

		@Override
		public boolean get(int i) {
			return bs.get(i);
		}

		@Override
		public void set(int idx, boolean b) {
			bs.set(idx, b);
		}

		@Override
		public int size() {
			return size;
		}
		
		public Object clone() {
			return new BitsetSignature(this);
		}


	}

}
