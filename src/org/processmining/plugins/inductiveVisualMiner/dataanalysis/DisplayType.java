package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.text.DecimalFormat;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;

/**
 * Class to display values, i.e. handles durations, times and aligns values.
 * 
 * @author sander
 *
 */
public abstract class DisplayType {

	/*
	 * Create using enum
	 * 
	 */

	public enum Type {
		numeric, duration, time, NA
	}

	public abstract Type getType();

	public static Type fromAttribute(Attribute attribute) {
		if (attribute.isNumeric()) {
			return Type.numeric;
		} else if (attribute.isTraceDuration()) {
			return Type.duration;
		} else if (attribute.isTraceNumberofEvents()) {
			return Type.numeric;
		} else {
			return Type.time;
		}
	}

	public static DisplayType create(Type type, double value) {
		if (value == -Double.MAX_VALUE) {
			return NA();
		}
		switch (type) {
			case duration :
				return duration(value);
			case numeric :
				return numeric(value);
			default :
				return null;
		}
	}

	public static DisplayType create(Type type, long value) {
		if (value == Long.MIN_VALUE) {
			return NA();
		}
		switch (type) {
			case numeric :
				return numeric(value);
			case time :
				return time(value);
			default :
				return null;
		}
	}

	/*
	 * static constructors
	 */

	public static Numeric numeric(double value) {
		return new Numeric(value);
	}

	public static NumericUnpadded numericUnpadded(long value) {
		return new NumericUnpadded(value);
	}

	public static Numeric numeric(long value) {
		return new Numeric(value);
	}

	public static Duration duration(double value) {
		return new Duration(value);
	}

	public static Time time(long value) {
		return new Time(value);
	}

	public static NA NA() {
		return new NA();
	}

	/*
	 * Display methods
	 */

	public static final DecimalFormat numberFormat = new DecimalFormat("0.0000");

	public abstract double getValue();

	public static class NA extends DisplayType {

		private NA() {
		}

		public String toString() {
			return "n/a" + "     ";
		}

		public Type getType() {
			return Type.NA;
		}

		public double getValue() {
			return -Double.MAX_VALUE;
		}

	}

	public static class NumericUnpadded extends DisplayType {
		long value;

		public NumericUnpadded(long value) {
			this.value = value;
		}

		public String toString() {
			return value + "";
		}

		public Type getType() {
			return Type.numeric;
		}

		public double getValue() {
			return value;
		}
	}

	public static class Numeric extends DisplayType {
		double valueDouble;
		long valueLong;

		private Numeric(double value) {
			valueDouble = value;
			valueLong = Long.MIN_VALUE;
		}

		private Numeric(long value) {
			valueDouble = -Double.MAX_VALUE;
			valueLong = value;
		}

		public String toString() {
			if (valueDouble != -Double.MAX_VALUE) {
				String s = numberFormat.format(valueDouble);
				s = s.replaceAll("0[ ]*$", " ");
				s = s.replaceAll("0([ ]*)$", " $1");
				s = s.replaceAll("0([ ]*)$", " $1");
				s = s.replaceAll("0([ ]*)$", " $1");
				s = s.replaceAll(".([ ]*)$", " $1");
				return s;
			} else {
				return valueLong + "     ";
			}
		}

		public double getValue() {
			return valueDouble != -Double.MAX_VALUE ? valueDouble : valueLong;
		}

		public Type getType() {
			return Type.numeric;
		}
	}

	public static class Duration extends DisplayType {
		double value;

		private Duration(double value) {
			this.value = value;
		}

		public String toString() {
			return ResourceTimeUtils.getDurationPadded(value);
		}

		public double getValue() {
			return value;
		}

		public Type getType() {
			return Type.numeric;
		}
	}

	public static class Time extends DisplayType {
		long value;

		private Time(long value) {
			this.value = value;
		}

		public String toString() {
			return ResourceTimeUtils.timeToString(value);
		}

		public double getValue() {
			return value;
		}

		public Type getType() {
			return Type.time;
		}
	}

}