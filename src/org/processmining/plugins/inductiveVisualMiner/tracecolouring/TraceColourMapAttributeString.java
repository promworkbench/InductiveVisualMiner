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

	private final Color[][] trace2colour;
	private final Map<String, Color> value2colour;
	private final Attribute attribute;

	public TraceColourMapAttributeString(IvMLogNotFiltered log, Attribute attribute, Map<String, Color> value2colour) {
		this.value2colour = value2colour;
		this.attribute = attribute;

		trace2colour = new Color[log.size()][256];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			XAttribute value = trace.getAttributes().get(attribute.getName());
			Color baseColour;
			if (value == null) {
				baseColour = TraceColourMapSettings.defaultColour;
			} else {
				baseColour = value2colour.get(value.toString());
			}
			for (int op = 0; op < 256; op++) {
				trace2colour[it.getPosition()][op] = new Color(baseColour.getRed(), baseColour.getGreen(),
						baseColour.getBlue(), op);
			}
		}
	}

	public Color getColour(int traceIndex, int opacity) {
		return trace2colour[traceIndex][opacity];
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
