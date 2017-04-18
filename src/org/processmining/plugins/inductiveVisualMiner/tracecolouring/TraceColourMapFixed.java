package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapFixed implements TraceColourMap {

	private final Color[] colour;

	public TraceColourMapFixed(Color colour) {
		this.colour = new Color[256];
		for (int op = 0; op < 256; op++) {
			this.colour[op] = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), op);
		}
	}

	public Color getColour(int traceIndex, int opacity) {
		return colour[opacity];
	}

	public Color getColour(IMTrace trace) {
		return colour[255];
	}

	public Color getColour(IvMTrace trace) {
		return colour[255];
	}

	public String getValue(IvMTrace trace) {
		return "";
	}

	public String getValue(IMTrace trace) {
		return "";
	}

}
