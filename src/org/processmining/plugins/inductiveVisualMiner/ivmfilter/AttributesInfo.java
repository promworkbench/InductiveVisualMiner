package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;

import gnu.trove.map.hash.THashMap;

public class AttributesInfo {

	private final THashMap<String, Attribute> traceAttributes;
	private final THashMap<String, Attribute> eventAttributes;

	public AttributesInfo(XLog log) {
		traceAttributes = new THashMap<>();
		Attribute traceDuration = new Attribute("duration", Attribute.Type.traceDuration);
		Attribute traceNumberOfEvents = new Attribute("number of events", Attribute.Type.traceNumberOfEvents);
		eventAttributes = new THashMap<>();

		for (XTrace trace : log) {
			add(traceAttributes, trace.getAttributes());

			long durationStart = Long.MAX_VALUE;
			long durationEnd = Long.MIN_VALUE;

			for (XEvent event : trace) {
				add(eventAttributes, event.getAttributes());
				Long timestamp = ResourceTimeUtils.getTimestamp(event);
				if (timestamp != null) {
					durationStart = Math.min(durationStart, timestamp);
					durationEnd = Math.max(durationEnd, timestamp);
				}
			}

			if (durationStart != Long.MAX_VALUE) {
				traceDuration.addTime(durationEnd - durationStart);
			}
			traceNumberOfEvents.addTime(trace.size());
		}

		//finalise
		for (Attribute attribute : traceAttributes.values()) {
			attribute.finalise();
		}
		for (Attribute attribute : eventAttributes.values()) {
			attribute.finalise();
		}
		while (traceAttributes.containsKey(traceDuration.getName())) {
			traceDuration.setName(traceDuration.getName() + " ");
		}
		traceAttributes.put(traceDuration.getName(), traceDuration);
		while (traceAttributes.containsKey(traceNumberOfEvents.getName())) {
			traceNumberOfEvents.setName(traceNumberOfEvents.getName() + " ");
		}
		traceAttributes.put(traceNumberOfEvents.getName(), traceNumberOfEvents);
	}

	private static void add(THashMap<String, Attribute> attributes, XAttributeMap add) {
		for (Entry<String, XAttribute> e : add.entrySet()) {
			Attribute old = attributes.get(e.getKey());
			if (old == null) {
				Attribute empty = new Attribute(e.getKey());
				empty.addValue(e.getValue());
				attributes.put(e.getKey(), empty);
			} else {
				old.addValue(e.getValue());
			}
		}
	}

	/**
	 * Convenience function for classifiers.
	 * 
	 * @return
	 */
	public Collection<Attribute> getEventAttributes() {
		return eventAttributes.values();
	}

	public Attribute getEventAttributeValues(String attribute) {
		return eventAttributes.get(attribute);
	}

	public Collection<Attribute> getTraceAttributes() {
		return new TreeSet<Attribute>(traceAttributes.values());
	}

	public Attribute getTraceAttributeValues(String attribute) {
		return traceAttributes.get(attribute);
	}
	
}
