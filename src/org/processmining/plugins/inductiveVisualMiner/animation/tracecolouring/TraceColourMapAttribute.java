package org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring;

import java.awt.Color;
import java.util.Map;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapAttribute implements TraceColourMap {

	private final Color[] trace2colour;
	public static final Color defaultColour = Color.yellow;
	private final Map<String, Color> value2colour;
	private final String attribute;
	
	public TraceColourMapAttribute(IvMLogNotFiltered log, TraceColourMapSettings settings) {
		this.value2colour = settings.value2colour;
		this.attribute = settings.attribute;
		
		trace2colour = new Color[log.size()];
		for (IteratorWithPosition<IvMTrace> it = log.iterator();it.hasNext();) {
			IvMTrace trace = it.next();
			XAttribute value = trace.getAttributes().get(attribute);
			if (value == null) {
				trace2colour[it.getPosition()] = defaultColour;
			} else {
				trace2colour[it.getPosition()] = value2colour.get(value.toString());
			}
		}
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
	}

	public Color getColour(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return defaultColour;
		} else {
			return value2colour.get(value.toString());
		}
	}

	public Color getColour(IvMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return defaultColour;
		} else {
			return value2colour.get(value.toString());
		}
	}

	public String getValue(IvMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return "";
		}
		return "█ " + value.toString();
	}

	public String getValue(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return "";
		}
		return "█ " + value.toString();
	}

	public static Color[] getColours(int numberOfColours) {
		switch (numberOfColours) {
			case 2 :
				return new Color[] { new Color(229, 245, 249), new Color(44, 162, 95) };
			case 3 :
				return new Color[] { new Color(229, 245, 249), new Color(153, 216, 201), new Color(44, 162, 95) };
			case 4 :
				return new Color[] { new Color(237, 248, 251), new Color(178, 226, 226), new Color(102, 194, 164),
						new Color(35, 139, 69) };
			default :
				return new Color[] { Color.yellow };
		}
	}
}
