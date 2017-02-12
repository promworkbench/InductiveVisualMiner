package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.util.Map;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapAttributeString implements TraceColourMap {

	private final Color[] trace2colour;
	private final Map<String, Color> value2colour;
	private final Attribute attribute;

	public TraceColourMapAttributeString(IvMLogNotFiltered log, Attribute attribute, Map<String, Color> value2colour) {
		this.value2colour = value2colour;
		this.attribute = attribute;

		trace2colour = new Color[log.size()];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			XAttribute value = trace.getAttributes().get(attribute);
			if (value == null) {
				trace2colour[it.getPosition()] = TraceColourMapSettings.defaultColour;
			} else {
				trace2colour[it.getPosition()] = value2colour.get(value.toString());
			}
		}
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
	}

	public Color getColour(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute.getName());
		if (value == null) {
			return TraceColourMapSettings.defaultColour;
		} else {
			return value2colour.get(value.toString());
		}
	}

	public Color getColour(IvMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute.getName());
		if (value == null) {
			return TraceColourMapSettings.defaultColour;
		} else {
			return value2colour.get(value.toString());
		}
	}

	public String getValue(IvMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute.getName());
		if (value == null) {
			return "";
		}
		return "\u2588 " + value.toString();
	}

	public String getValue(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute.getName());
		if (value == null) {
			return "";
		}
		return "\u2588 " + value.toString();
	}
}
