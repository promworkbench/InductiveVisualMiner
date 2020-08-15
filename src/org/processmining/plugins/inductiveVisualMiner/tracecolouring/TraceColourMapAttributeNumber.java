package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class TraceColourMapAttributeNumber implements TraceColourMap {

	private final Attribute attribute;
	private final Color[] trace2colour;
	private final double min;
	private final double max;
	private final ColourMap map;

	public TraceColourMapAttributeNumber(IvMLogNotFiltered log, Attribute attribute, ColourMap map, double min,
			double max) {
		this.attribute = attribute;
		this.min = min;
		this.max = max;
		this.map = map;

		trace2colour = new Color[log.size()];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			trace2colour[it.getPosition()] = attributeValue2colour(attribute, trace);
		}
	}

	public Color attributeValue2colour(Attribute attribute, XAttributable trace) {
		if (attribute == null) {
			return RendererFactory.defaultTokenFillColour;
		} else {
			double value = attribute.getNumeric(trace);
			if (value == -Double.MAX_VALUE) {
				return RendererFactory.defaultTokenFillColour;
			}

			return map.colour(value, min, max);
		}
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
	}

	public Color getColour(IvMTrace trace) {
		return attributeValue2colour(attribute, trace);
	}

	public String getValue(IvMTrace trace) {
		double value = attribute.getNumeric(trace);
		if (value == -Double.MAX_VALUE) {
			return "";
		}
		return "\u2588 " + value;
	}
}