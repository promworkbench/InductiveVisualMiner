package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import gnu.trove.set.hash.THashSet;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.math.NumberUtils;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeTimestamp;

public class Attribute implements Comparable<Attribute> {
	public enum Type {
		undecided, literal, numeric, time, traceDuration, traceNumberOfEvents;
	}

	private String name;
	private Type type = Type.undecided;
	private Collection<String> valuesLiteral;
	private double valuesNumericMin;
	private double valuesNumericMax;
	private long valuesTimeMin;
	private long valuesTimeMax;

	public Attribute(String name) {
		this.name = name;
		this.valuesLiteral = new THashSet<>();
		this.valuesNumericMin = Double.MAX_VALUE;
		this.valuesNumericMax = Double.MIN_VALUE;
		this.valuesTimeMin = Long.MAX_VALUE;
		this.valuesTimeMax = Long.MIN_VALUE;
	}

	public Attribute(String name, Type type) {
		this.name = name;
		this.type = type;
		switch (type) {
			case traceDuration :
				valuesNumericMin = Double.MAX_VALUE;
				valuesNumericMax = Double.MIN_VALUE;
				break;
			case literal :
				valuesLiteral = new THashSet<>();
				break;
			case numeric :
				valuesNumericMin = Double.MAX_VALUE;
				valuesNumericMax = Double.MIN_VALUE;
				break;
			case time :
				valuesTimeMin = Long.MAX_VALUE;
				valuesTimeMax = Long.MIN_VALUE;
				break;
			case undecided :
				break;
			case traceNumberOfEvents :
				valuesNumericMin = Double.MAX_VALUE;
				valuesNumericMax = Double.MIN_VALUE;
				break;
			default :
				break;
		}
	}

	public void addValue(XAttribute attribute) {
		valuesLiteral.add(attribute.toString());
		if (type == type.undecided) {
			double numeric = parseDoubleFast(attribute);
			if (numeric != Double.MIN_VALUE) {
				type = type.numeric;
			} else {
				long time = parseTimeFast(attribute);
				if (time != Long.MIN_VALUE) {
					type = type.time;
				} else {
					type = type.literal;
				}
			}
		}
		//process numeric
		if (type == type.numeric) {
			double numeric = parseDoubleFast(attribute);
			if (numeric != Double.MIN_VALUE) {
				//this is a number
				valuesNumericMin = Math.min(valuesNumericMin, numeric);
				valuesNumericMax = Math.max(valuesNumericMax, numeric);
			} else {
				//this is a string, remove the number storage
				type = Type.literal;
			}
		}
		//process time
		if (type == type.time) {
			long time = parseTimeFast(attribute);
			if (time != Long.MIN_VALUE) {
				//this is a time
				valuesTimeMin = Math.min(valuesTimeMin, time);
				valuesTimeMax = Math.max(valuesTimeMax, time);
			} else {
				//this is a string, remove the number storage
				type = Type.literal;
			}
		}
	}

	public void addNumber(double number) {
		assert (type == type.numeric);
		valuesNumericMin = Math.min(valuesNumericMin, number);
		valuesNumericMax = Math.max(valuesNumericMax, number);
	}

	public void addTime(long time) {
		assert (type == type.time || type == type.traceDuration || type == type.traceNumberOfEvents);
		valuesTimeMin = Math.min(valuesTimeMin, time);
		valuesTimeMax = Math.max(valuesTimeMax, time);
	}

	public void finalise() {
		switch (type) {
			case literal :
				// sort the values
				valuesLiteral = new ArrayList<String>(valuesLiteral);
				Collections.sort((ArrayList<String>) valuesLiteral);
				break;
			default :
				valuesLiteral = null;
				break;
		}
	}

	public int compareTo(Attribute arg0) {
		return name.toLowerCase().compareTo(arg0.getName().toLowerCase());
	}

	public String toString() {
		return name;
	}

	public boolean isLiteral() {
		return type == Type.literal;
	}

	public boolean isNumeric() {
		return type == Type.numeric;
	}

	public boolean isTime() {
		return type == Type.time;
	}

	public boolean isTraceDuration() {
		return type == Type.traceDuration;
	}

	public boolean isTraceNumberofEvents() {
		return type == Type.traceNumberOfEvents;
	}

	public Collection<String> getStringValues() {
		return valuesLiteral;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getNumericMin() {
		assert (type == type.numeric);
		return valuesNumericMin;
	}

	public double getNumericMax() {
		assert (type == type.numeric);
		return valuesNumericMax;
	}

	public long getTimeMin() {
		assert (type == type.time || type == type.traceDuration || type == type.traceNumberOfEvents);
		return valuesTimeMin;
	}

	public long getTimeMax() {
		assert (type == type.time || type == type.traceDuration || type == type.traceNumberOfEvents);
		return valuesTimeMax;
	}

	public static long parseTimeFast(XAttribute attribute) {
		if (attribute instanceof XAttributeTimestamp) {
			return ((XAttributeTimestamp) attribute).getValueMillis();
		}
		return Long.MIN_VALUE;
	}

	/**
	 * See if the given attribute has a numeric value. Returns Double.MIN_VALUE
	 * if not.
	 * 
	 * @param attribute
	 * @return
	 */
	public static double parseDoubleFast(XAttribute attribute) {
		if (attribute instanceof XAttributeDiscrete || attribute instanceof XAttributeContinuous) {
			//the attribute was declared to be a number
			if (attribute instanceof XAttributeDiscrete) {
				return ((XAttributeDiscrete) attribute).getValue();
			} else {
				return ((XAttributeContinuous) attribute).getValue();
			}
		} else if (isStringNumeric(attribute.toString())) {
			//the attribute was declared to be a string, check if it is not a number anyway
			return NumberUtils.toDouble(attribute.toString(), Double.MIN_VALUE);
		}
		return Double.MIN_VALUE;
	}

	public static boolean isStringNumeric(String str) {
		DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
		char localeMinusSign = currentLocaleSymbols.getMinusSign();

		if (str.isEmpty() || !Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign) {
			return false;
		}

		boolean isDecimalSeparatorFound = false;
		char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

		for (char c : str.substring(1).toCharArray()) {
			if (!Character.isDigit(c)) {
				if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
					isDecimalSeparatorFound = true;
					continue;
				}
				return false;
			}
		}
		return true;
	}
}
