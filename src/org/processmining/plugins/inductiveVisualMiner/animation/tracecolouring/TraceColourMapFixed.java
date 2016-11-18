package org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring;

import java.awt.Color;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapFixed implements TraceColourMap {

	private final Color colour;
	
	public TraceColourMapFixed(Color colour) {
		this.colour = colour;
	}
	
	public Color getColour(int traceIndex) {
		return colour;
	}

	public Color getColour(IMTrace trace) {
		return colour;
	}

	public Color getColour(IvMTrace trace) {
		return colour;
	}

	public String getValue(IvMTrace trace) {
		return "";
	}

	public String getValue(IMTrace trace) {
		return "";
	}

}
