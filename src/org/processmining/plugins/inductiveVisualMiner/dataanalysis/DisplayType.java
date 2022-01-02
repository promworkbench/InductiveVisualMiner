package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.JLabel;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceUtils;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

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
		numeric, bool, duration, time, literal, html, image, NA
	}

	public abstract Type getType();

	public static Type fromAttribute(Attribute attribute) {
		if (attribute.isNumeric()) {
			return Type.numeric;
		} else if (attribute.isBoolean()) {
			return Type.bool;
		} else if (attribute.isDuration()) {
			return Type.duration;
		} else if (attribute.isTime()) {
			return Type.time;
		} else {
			return Type.literal;
		}
	}

	public static DisplayType create(Type type, double value) {
		if (value == -Double.MAX_VALUE) {
			return NA();
		}
		switch (type) {
			case numeric :
				return numeric(value);
			default :
				assert false; //a double cannot represent time or duration, as it is inaccurate
				return null;
		}
	}

	public static DisplayType create(Type type, Boolean value) {
		if (value == null) {
			return NA();
		}
		switch (type) {
			case bool :
				return bool(value);
			default :
				assert false; //a double cannot represent time or duration, as it is inaccurate
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
			case duration :
				return duration(value);
			default :
				assert false;
				return null;
		}
	}

	public static DisplayType create(Type type, String value) {
		if (value == null) {
			return NA();
		}
		switch (type) {
			case literal :
				return literal(value);
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

	public static Bool bool(boolean value) {
		return new Bool(value);
	}

	public static NumericUnpadded numericUnpadded(long value) {
		return new NumericUnpadded(value);
	}

	public static Numeric numeric(long value) {
		return new Numeric(value);
	}

	public static Duration duration(long value) {
		return new Duration(value);
	}

	public static Time time(long value) {
		return new Time(value);
	}

	public static Literal literal(String value) {
		return new Literal(value);
	}

	public static HTML html(String value) {
		return new HTML(value);
	}

	public static Image image(BufferedImage value) {
		return new Image(value);
	}

	public static NA NA() {
		return new NA();
	}

	/*
	 * Display methods
	 */

	public static final DecimalFormat numberFormat = new DecimalFormat("0.0000");

	public abstract double getValue();

	/*
	 * Non-abstract methods
	 */

	private int horizontalAlignment = JLabel.RIGHT;

	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/*
	 * Subclasses
	 */

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

		public long getValueLong() {
			return valueLong;
		}

		public Type getType() {
			return Type.numeric;
		}
	}

	public static class Bool extends DisplayType {
		boolean value;

		private Bool(boolean value) {
			this.value = value;
		}

		public String toString() {
			return Boolean.toString(value);
		}

		public double getValue() {
			return -Double.MAX_VALUE;
		}

		public Type getType() {
			return Type.bool;
		}
	}

	public static class Duration extends DisplayType {
		long value;

		private Duration(long value) {
			this.value = value;
		}

		public String toString() {
			return PerformanceUtils.timeToString(value);
		}

		public double getValue() {
			return value;
		}

		public long getValueLong() {
			return value;
		}

		public Type getType() {
			return Type.duration;
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

		public long getValueLong() {
			return value;
		}

		public Type getType() {
			return Type.time;
		}

	}

	public static class Literal extends DisplayType {
		String value;

		private Literal(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

		public double getValue() {
			return -Double.MAX_VALUE;
		}

		public Type getType() {
			return Type.literal;
		}

	}

	public static class HTML extends DisplayType {
		String value;

		private HTML(String value) {
			this.value = value;
		}

		public String toString() {
			return "<html>" + value + "</html>";
		}

		public double getValue() {
			return -Double.MAX_VALUE;
		}

		public String getRawValue() {
			return value //
					.replace("&lt;", "<") //
					.replace("&gt;", ">");
		}

		public Type getType() {
			return Type.html;
		}

	}

	public static class Image extends DisplayType {
		BufferedImage value;

		private Image(BufferedImage value) {
			this.value = value;
		}

		public String toString() {
			return "";
		}

		public double getValue() {
			return -Double.MAX_VALUE;
		}

		public Type getType() {
			return Type.image;
		}

		public BufferedImage getImage() {
			return value;
		}
	}
}