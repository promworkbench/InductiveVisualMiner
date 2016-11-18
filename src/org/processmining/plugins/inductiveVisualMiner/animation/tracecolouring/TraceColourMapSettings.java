package org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring;

import java.awt.Color;
import java.util.Map;

public class TraceColourMapSettings {
	public final String attribute;
	public final int numberOfColours;
	public final Color[] colours;
	public final Map<String, Color> value2colour;

	public static TraceColourMapSettings empty() {
		return new TraceColourMapSettings(null, -1, null, null);
	}

	public TraceColourMapSettings(String attribute, int numberOfColours, Color[] colours,
			Map<String, Color> value2colour) {
		this.attribute = attribute;
		this.numberOfColours = numberOfColours;
		this.colours = colours;
		this.value2colour = value2colour;
	}
}
