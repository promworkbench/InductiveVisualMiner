package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.util.Map;

import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMapViridis;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class TraceColourMapSettings {

	private enum Type {
		empty, attributeString, attributeNumber, attributeTime, propertyNumberOfEvents, propertyTraceDuration
	}

	private final Type type;

	//string
	private final Attribute attribute;
	private final ColourMap colourMap;
	private final Map<String, Color> value2colour;

	//number
	private final double min;
	private final double max;

	public static TraceColourMapSettings empty() {
		return new TraceColourMapSettings(Type.empty, null, null, null, Double.MIN_VALUE, Double.MIN_VALUE);
	}

	public static TraceColourMapSettings string(Attribute attribute, Map<String, Color> value2colour) {
		return new TraceColourMapSettings(Type.attributeString, attribute, null, value2colour,
				Double.MIN_VALUE, Double.MIN_VALUE);
	}

	public static TraceColourMapSettings number(Attribute attribute, ColourMap colourMap, double min, double max) {
		return new TraceColourMapSettings(Type.attributeNumber, attribute, colourMap, null, min, max);
	}

	public static TraceColourMapSettings time(Attribute attribute, ColourMap colourMap, long min, long max) {
		return new TraceColourMapSettings(Type.attributeTime, attribute, colourMap, null, min, max);
	}

	public static TraceColourMapSettings duration(ColourMap colourMap, long min, long max) {
		return new TraceColourMapSettings(Type.propertyTraceDuration, null, colourMap, null, min, max);
	}

	public static TraceColourMapSettings numberOfEvents(ColourMap colourMap, long min, long max) {
		return new TraceColourMapSettings(Type.propertyNumberOfEvents, null, colourMap, null, min, max);
	}

	private TraceColourMapSettings(Type type, Attribute attribute, ColourMap colourMap,
			Map<String, Color> value2colour, double min, double max) {
		this.type = type;
		this.attribute = attribute;
		this.colourMap = colourMap;
		this.value2colour = value2colour;
		this.min = min;
		this.max = max;
	}

	/**
	 * Must be called asynchronously, as it takes a long time (sets up trace
	 * colour map for log).
	 * 
	 * @param log
	 * @return
	 */
	public TraceColourMap getTraceColourMap(IvMModel model, IvMLogNotFiltered log) {
		switch (type) {
			case attributeNumber :
				return new TraceColourMapAttributeNumber(log, attribute, colourMap, min, max);
			case attributeString :
				return new TraceColourMapAttributeString(log, attribute, value2colour);
			case attributeTime :
				return new TraceColourMapAttributeTime(log, attribute, colourMap, (long) min, (long) max);
			case empty :
				return new TraceColourMapFixed(RendererFactory.defaultTokenFillColour);
			case propertyNumberOfEvents :
				return new TraceColourMapPropertyNumberOfEvents(model, log, colourMap, min, max);
			case propertyTraceDuration :
				return new TraceColourMapPropertyDuration(model, log, colourMap, min, max);
			default :
				return new TraceColourMapFixed(RendererFactory.defaultTokenFillColour);
		}
	}

	public static Color[] getColours(int numberOfColours) {
		switch (numberOfColours) {
			case 1 :
				return new Color[] { new Color(224, 236, 244) };
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
				return new Color[] { RendererFactory.defaultTokenFillColour };
		}
	}

	public static ColourMap getColourMap() {
		return new ColourMapViridis();
	}
}
