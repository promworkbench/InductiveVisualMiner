package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.custom_hash.TObjectDoubleCustomHashMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.HashingStrategy;

public class AttributesInfo {

	private final String[] eventAttributes;
	private final THashMap<String, THashSet<String>> eventAttributesMap;
	private final THashMap<String, Type> eventAttributesTypes;
	private final TObjectDoubleMap<String> eventAttributesMin;
	private final TObjectDoubleMap<String> eventAttributesMax;
	private final String[] traceAttributes;
	private final THashMap<String, THashSet<String>> traceAttributesMap;
	private final THashMap<String, Type> traceAttributesTypes;
	private final TObjectDoubleMap<String> traceAttributesMin;
	private final TObjectDoubleMap<String> traceAttributesMax;

	private final long traceNumberOfEventsMin;
	private final long traceNumberOfEventsMax;
	private final long traceDurationMin;
	private final long traceDurationMax;

	private enum Type {
		string, number, time;
	}

	public AttributesInfo(XLog log) {
		traceAttributesMap = new THashMap<>();
		traceAttributesTypes = new THashMap<>();
		traceAttributesMin = createExtremesMap(Double.MAX_VALUE);
		traceAttributesMax = createExtremesMap(Double.MIN_VALUE);
		eventAttributesMap = new THashMap<>();
		eventAttributesTypes = new THashMap<>();
		eventAttributesMin = createExtremesMap(Double.MAX_VALUE);
		eventAttributesMax = createExtremesMap(Double.MIN_VALUE);

		long traceNumberOfEventsMin = Long.MAX_VALUE;
		long traceNumberOfEventsMax = 0;
		long traceDurationMin = Long.MAX_VALUE;
		long traceDurationMax = Long.MIN_VALUE;

		for (XTrace trace : log) {
			add(traceAttributesMap, traceAttributesTypes, traceAttributesMin, traceAttributesMax,
					trace.getAttributes());

			long durationStart = Long.MAX_VALUE;
			long durationEnd = Long.MIN_VALUE;

			for (XEvent event : trace) {
				add(eventAttributesMap, eventAttributesTypes, eventAttributesMin, eventAttributesMax,
						event.getAttributes());
				Long timestamp = ResourceTimeUtils.getTimestamp(event);
				if (timestamp != null) {
					durationStart = Math.min(durationStart, timestamp);
					durationEnd = Math.max(durationEnd, timestamp);
				}
			}

			if (durationStart != Long.MAX_VALUE) {
				traceDurationMin = Math.min(traceDurationMin, durationEnd - durationStart);
				traceDurationMax = Math.max(traceDurationMax, durationEnd - durationStart);
			}

			if (trace.size() < traceNumberOfEventsMin) {
				traceNumberOfEventsMin = trace.size();
			}
			if (trace.size() > traceNumberOfEventsMax) {
				traceNumberOfEventsMax = trace.size();
			}
		}

		//finalise
		this.eventAttributes = eventAttributesMap.keySet().toArray(new String[eventAttributesMap.size()]);
		Arrays.sort(this.eventAttributes);
		this.traceAttributes = traceAttributesMap.keySet().toArray(new String[traceAttributesMap.size()]);
		Arrays.sort(this.traceAttributes);
		this.traceNumberOfEventsMin = traceNumberOfEventsMin;
		this.traceNumberOfEventsMax = traceNumberOfEventsMax;
		this.traceDurationMin = traceDurationMin;
		this.traceDurationMax = traceDurationMax;
	}

	public static void add(THashMap<String, THashSet<String>> attributes, THashMap<String, Type> types,
			TObjectDoubleMap<String> min, TObjectDoubleMap<String> max, XAttributeMap add) {
		for (Entry<String, XAttribute> e : add.entrySet()) {
			THashSet<String> empty = new THashSet<>();
			THashSet<String> old = attributes.putIfAbsent(e.getKey(), empty);
			if (old == null) {
				//key was not present
				empty.add(e.getValue().toString());
				types.put(e.getKey(), Type.number);
			} else {
				old.add(e.getValue().toString());
			}

			if (types.get(e.getKey()) != Type.string) {

				double value = parseDoubleFast(e.getValue());
				if (value != Double.MIN_VALUE) {
					//the attribute is a number
					if (value < min.get(e.getKey())) {
						min.put(e.getKey(), value);
					}
					if (value > max.get(e.getKey())) {
						max.put(e.getKey(), value);
					}
				} else {
					types.put(e.getKey(), Type.string);
				}
			} else {
				types.put(e.getKey(), Type.string);
			}
		}
	}

	/**
	 * Convenience function for classifiers.
	 * 
	 * @return
	 */
	public String[] getEventAttributes() {
		return eventAttributes;
	}

	public Map<String, ? extends Set<String>> getEventAttributesMap() {
		return eventAttributesMap;
	}

	public String[] getTraceAttributes() {
		return traceAttributes;
	}

	public Map<String, ? extends Set<String>> getTraceAttributesMap() {
		return traceAttributesMap;
	}

	public boolean isTraceAttributeNumeric(String traceAttribute) {
		return traceAttributesTypes.get(traceAttribute) == Type.number;
	}

	public double getTraceAttributeMinimum(String traceAttribute) {
		return traceAttributesMin.get(traceAttribute);
	}

	public double getTraceAttributeMaximum(String traceAttribute) {
		return traceAttributesMax.get(traceAttribute);
	}

	private static TObjectDoubleCustomHashMap<String> createExtremesMap(double nonPresentValue) {
		return new TObjectDoubleCustomHashMap<String>(new HashingStrategy<String>() {
			private static final long serialVersionUID = 5978383264312795719L;

			public int computeHashCode(String object) {
				return object.hashCode();
			}

			public boolean equals(String o1, String o2) {
				return o1.equals(o2);
			}
		}, 10, 0.5f, nonPresentValue);
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

		if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign) {
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

	public long getTraceNumberOfEventsMin() {
		return traceNumberOfEventsMin;
	}

	public long getTraceNumberOfEventsMax() {
		return traceNumberOfEventsMax;
	}

	public long getTraceDurationMin() {
		return traceDurationMin;
	}

	public long getTraceDurationMax() {
		return traceDurationMax;
	}
}
