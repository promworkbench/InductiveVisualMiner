package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.text.DecimalFormat;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public abstract class TraceColourMapProperty implements TraceColourMap {

	private final double min;
	private final double max;
	private final Color[] trace2colour;
	private final ColourMap colourMap;
	protected final IvMModel model;

	public TraceColourMapProperty(IvMModel model, IvMLogNotFiltered log, ColourMap colourMap, double min, double max) {
		this.min = min;
		this.max = max;
		this.colourMap = colourMap;
		this.model = model;

		trace2colour = new Color[log.size()];
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			trace2colour[it.getPosition()] = attributeValue2colour(getProperty(trace));
		}
	}

	protected Color attributeValue2colour(double value) {
		if (max != min) {
			return colourMap.colour(value, min, max);
		} else {
			return RendererFactory.defaultTokenFillColour;
		}
	}

	/**
	 * 
	 * @param trace
	 * @return Double.MIN_VALUE if not present
	 */
	protected abstract double getProperty(IvMTrace trace);

	/**
	 * 
	 * @param trace
	 * @return Double.MIN_VALUE if not present
	 */
	protected abstract double getProperty(IMTrace trace);

	public Color getColour(IMTrace trace) {
		return attributeValue2colour(getProperty(trace));
	}

	public Color getColour(IvMTrace trace) {
		return attributeValue2colour(getProperty(trace));
	}

	public Color getColour(int traceIndex) {
		return trace2colour[traceIndex];
	}

	final private static DecimalFormat numberFormat = new DecimalFormat("#.##");

	public String getValue(IvMTrace trace) {
		if (getProperty(trace) > Double.MIN_VALUE) {
			return "\u2588 " + numberFormat.format(getProperty(trace));
		} else {
			return "";
		}
	}

	public String getValue(IMTrace trace) {
		if (getProperty(trace) > Double.MIN_VALUE) {
			return "\u2588 " + numberFormat.format(getProperty(trace));
		} else {
			return "";
		}
	}

}
