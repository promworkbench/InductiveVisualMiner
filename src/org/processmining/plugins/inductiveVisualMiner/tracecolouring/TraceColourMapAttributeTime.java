package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapAttributeTime implements TraceColourMap {

	private final Attribute attribute;
	private final Color[][] trace2colour;
	private final long min;
	private final long max;
	private final Color[] colours;

	public TraceColourMapAttributeTime(IvMLogNotFiltered log, Attribute attribute, Color[] colours, long min,
			long max) {
		this.attribute = attribute;
		this.min = min;
		this.max = max;
		this.colours = colours;

		trace2colour = new Color[log.size()][256];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			Color baseColour = attributeValue2colour(trace.getAttributes().get(attribute.getName()));
			for (int op = 0; op < 256; op++) {
				trace2colour[it.getPosition()][op] = new Color(baseColour.getRed(), baseColour.getGreen(),
						baseColour.getBlue(), op);
			}
		}
	}

	public Color attributeValue2colour(XAttribute attribute) {
		if (attribute == null) {
			return TraceColourMapSettings.defaultColour;
		} else {
			long value = Attribute.parseTimeFast(attribute);
			if (value == Long.MIN_VALUE) {
				return TraceColourMapSettings.defaultColour;
			}

			return colours[(int) (Math.min(colours.length * (value - min) / (max - min), colours.length - 1.0))];
		}
	}

	public Color getColour(int traceIndex, int opacity) {
		return trace2colour[traceIndex][opacity];
	}

	public Color getColour(IMTrace trace) {
		return attributeValue2colour(trace.getAttributes().get(attribute.getName()));
	}

	public Color getColour(IvMTrace trace) {
		return attributeValue2colour(trace.getAttributes().get(attribute.getName()));
	}

	public String getValue(IvMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute.getName());
		if (value == null) {
			return "";
		}
		return "\u2588 " + ResourceTimeUtils.timeToString(Attribute.parseTimeFast(value));
	}

	public String getValue(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute.getName());
		if (value == null) {
			return "";
		}
		return "\u2588 " + ResourceTimeUtils.timeToString(Attribute.parseTimeFast(value));
	}
}
