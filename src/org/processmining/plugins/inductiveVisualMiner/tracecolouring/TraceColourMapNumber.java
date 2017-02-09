package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapNumber implements TraceColourMap {

	private final String attribute;
	private final Color[] trace2colour;
	private final double min;
	private final double max;
	private final Color[] colours;

	public TraceColourMapNumber(IvMLogNotFiltered log, String attribute, Color[] colours, double min, double max) {
		this.attribute = attribute;
		this.min = min;
		this.max = max;
		this.colours = colours;

		trace2colour = new Color[log.size()];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			trace2colour[it.getPosition()] = attributeValue2colour(trace.getAttributes().get(attribute));
		}
	}

	public Color attributeValue2colour(XAttribute attribute) {
		if (attribute == null) {
			return TraceColourMapSettings.defaultColour;
		} else {
			double value = AttributesInfo.parseDoubleFast(attribute);
			if (value == Double.MIN_VALUE) {
				return TraceColourMapSettings.defaultColour;
			}

			return colours[(int) (Math.min(colours.length * (value - min) / (max - min), colours.length - 1.0))];
		}
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
	}

	public Color getColour(IMTrace trace) {
		return attributeValue2colour(trace.getAttributes().get(attribute));
	}

	public Color getColour(IvMTrace trace) {
		return attributeValue2colour(trace.getAttributes().get(attribute));
	}

	public String getValue(IvMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return "";
		}
		return "\u2588 " + value.toString();
	}

	public String getValue(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return "";
		}
		return "\u2588 " + value.toString();
	}
}
