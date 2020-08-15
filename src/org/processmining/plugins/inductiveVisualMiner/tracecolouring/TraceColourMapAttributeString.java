package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.util.Map;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

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
			String value = attribute.getLiteral(trace);
			Color baseColour;
			if (value == null) {
				baseColour = RendererFactory.defaultTokenFillColour;
			} else {
				baseColour = value2colour.get(value);
			}
			trace2colour[it.getPosition()] = baseColour;
		}
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
	}

	public Color getColour(IvMTrace trace) {
		String value = attribute.getLiteral(trace);
		if (value == null) {
			return RendererFactory.defaultTokenFillColour;
		} else {
			return value2colour.get(value);
		}
	}

	public String getValue(IvMTrace trace) {
		String value = attribute.getLiteral(trace);
		if (value == null) {
			return "";
		}
		return "\u2588 " + value;
	}
}
