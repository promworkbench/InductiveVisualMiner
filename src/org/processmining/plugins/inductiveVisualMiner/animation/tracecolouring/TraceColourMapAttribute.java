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
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
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
		return "\u2588 " + value.toString();
	}

	public String getValue(IMTrace trace) {
		XAttribute value = trace.getAttributes().get(attribute);
		if (value == null) {
			return "";
		}
		return "\u2588 " + value.toString();
	}

	public static Color[] getColours(int numberOfColours) {
		switch (numberOfColours) {
			case 2 :
				return new Color[] { new Color(224, 236, 244), new Color(136, 86, 167) };
			case 3 :
				return new Color[] { new Color(224, 236, 244), new Color(158, 188, 218), new Color(136, 86, 167) };
			case 4 :
				return new Color[] { new Color(237, 248, 251), new Color(179, 205, 227), new Color(140, 150, 198),
						new Color(136, 65, 157) };
			case 5 :
				return new Color[] { new Color(237, 248, 251), new Color(179, 205, 227), new Color(140, 150, 198),
						new Color(136, 86, 167), new Color(129, 15, 124) };
			case 6 :
				return new Color[] { new Color(237, 248, 251), new Color(191, 211, 230), new Color(158, 188, 218),
						new Color(140, 150, 198), new Color(136, 86, 167), new Color(129, 15, 124) };
			case 7 :
				return new Color[] { new Color(237, 248, 251), new Color(191, 211, 230), new Color(158, 188, 218),
						new Color(140, 150, 198), new Color(140, 107, 177), new Color(136, 65, 157),
						new Color(110, 1, 107) };
			default :
				return new Color[] { Color.yellow };
		}
	}
}
