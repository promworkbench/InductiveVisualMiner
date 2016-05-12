package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class AttributesInfo {

	private final String[] eventAttributes;
	private final THashMap<String, THashSet<String>> eventAttributesMap;
	private final THashMap<String, THashSet<String>> traceAttributesMap;

	public AttributesInfo(XLog log) {
		traceAttributesMap = new THashMap<>();
		eventAttributesMap = new THashMap<>();
		for (XTrace trace : log) {
			add(traceAttributesMap, trace.getAttributes());
			for (XEvent event : trace) {
				add(eventAttributesMap, event.getAttributes());
			}
		}

		//finalise
		this.eventAttributes = eventAttributesMap.keySet().toArray(new String[eventAttributesMap.size()]);
		Arrays.sort(this.eventAttributes);
	}

	public static void add(THashMap<String, THashSet<String>> attributes, XAttributeMap add) {
		for (Entry<String, XAttribute> e : add.entrySet()) {
			THashSet<String> empty = new THashSet<>();
			THashSet<String> old = attributes.putIfAbsent(e.getKey(), empty);
			if (old == null) {
				//was not present
				empty.add(e.getValue().toString());
			} else {
				old.add(e.getValue().toString());
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

	public Map<String, ? extends Set<String>> getTraceAttributesMap() {
		return traceAttributesMap;
	}
}
