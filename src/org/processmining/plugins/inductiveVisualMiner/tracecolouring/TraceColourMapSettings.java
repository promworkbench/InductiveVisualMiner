package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.util.Map;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class TraceColourMapSettings {

	public static final Color defaultColour = Color.yellow;

	//string
	private final String attribute;
	private final Color[] colours;
	private final Map<String, Color> value2colour;

	//number
	private final double min;
	private final double max;

	public static TraceColourMapSettings empty() {
		return new TraceColourMapSettings(null, null, null, Double.MIN_VALUE, Double.MIN_VALUE);
	}

	public static TraceColourMapSettings string(String attribute, Color[] colours,
			Map<String, Color> value2colour) {
		return new TraceColourMapSettings(attribute, colours, value2colour, Double.MIN_VALUE,
				Double.MIN_VALUE);
	}
	
	public static TraceColourMapSettings number(String attribute, Color[] colours, double min, double max) {
		return new TraceColourMapSettings(attribute, colours, null, min, max);
	}

	private TraceColourMapSettings(String attribute, Color[] colours,
			Map<String, Color> value2colour, double min, double max) {
		this.attribute = attribute;
		this.colours = colours;
		this.value2colour = value2colour;
		this.min = min;
		this.max = max;
	}

	/**
	 * Must be called asynchronously, as it takes a long time (sets up trace colour map for log).
	 * @param log
	 * @return
	 */
	public TraceColourMap getTraceColourMap(IvMLogNotFiltered log) {
		if (colours == null || colours.length < 1 || attribute == null) {
			return new TraceColourMapFixed(Color.yellow);
		} else if (min != Double.MIN_VALUE) {
			return new TraceColourMapNumber(log, attribute, colours, min, max);
		} else {
			return new TraceColourMapString(log, attribute, value2colour);
		}
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
