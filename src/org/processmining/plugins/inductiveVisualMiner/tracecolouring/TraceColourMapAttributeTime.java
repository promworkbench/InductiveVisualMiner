package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class TraceColourMapAttributeTime implements TraceColourMap {

	private final Attribute attribute;
	private final Color[] trace2colour;
	private final long min;
	private final long max;
	private final ColourMap colourMap;

	public TraceColourMapAttributeTime(IvMLogNotFiltered log, Attribute attribute, ColourMap colourMap, long min,
			long max) {
		this.attribute = attribute;
		this.min = min;
		this.max = max;
		this.colourMap = colourMap;

		trace2colour = new Color[log.size()];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			trace2colour[it.getPosition()] = attributeValue2colour(trace.getAttributes().get(attribute.getName()));
		}
	}

	public Color attributeValue2colour(XAttribute attribute) {
		if (attribute == null) {
			return RendererFactory.defaultTokenFillColour;
		} else {
			long value = Attribute.parseTimeFast(attribute);
			if (value == Long.MIN_VALUE) {
				return RendererFactory.defaultTokenFillColour;
			}

			return colourMap.colour(value, min, max);
		}
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
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
