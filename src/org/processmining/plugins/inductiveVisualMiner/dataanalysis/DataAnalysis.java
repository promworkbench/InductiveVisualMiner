package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapPropertyDuration;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class DataAnalysis {

	private final TObjectDoubleMap<Attribute> attribute2correlation;
	private final Map<Attribute, BufferedImage> attribute2correlationDensityPlot;
	private final TObjectDoubleMap<Attribute> attribute2correlationNegative;
	private final Map<Attribute, BufferedImage> attribute2correlationDensityPlotNegative;

	private final boolean isSomethingFiltered;

	public DataAnalysis(IvMLogFiltered logFiltered, AttributesInfo attributes) throws CloneNotSupportedException {
		isSomethingFiltered = logFiltered.isSomethingFiltered();

		Pair<TObjectDoubleMap<Attribute>, Map<Attribute, BufferedImage>> p1 = get(logFiltered, attributes);
		attribute2correlation = p1.getA();
		attribute2correlationDensityPlot = p1.getB();

		IvMLogFilteredImpl logFilteredNegative = logFiltered.clone();
		logFilteredNegative.invert();
		Pair<TObjectDoubleMap<Attribute>, Map<Attribute, BufferedImage>> p2 = get(logFilteredNegative, attributes);
		attribute2correlationNegative = p2.getA();
		attribute2correlationDensityPlotNegative = p2.getB();
	}

	public static Pair<TObjectDoubleMap<Attribute>, Map<Attribute, BufferedImage>> get(IvMLogFiltered logFiltered,
			AttributesInfo attributes) {

		//count the number of traces
		int numberOfTraces = 0;
		{
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				it.next();
				numberOfTraces++;
			}
		}

		//compute fitness
		double[] fitness = new double[numberOfTraces];
		{
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();

				fitness[i] = Fitness.compute(trace);
				i++;
			}
		}

		//compute correlation
		TObjectDoubleHashMap<Attribute> correlationMap = new TObjectDoubleHashMap<>(10, 0.5f, Double.MIN_VALUE);
		THashMap<Attribute, BufferedImage> imageMap = new THashMap<>();
		for (Attribute attribute : attributes.getTraceAttributes()) {

			if (attribute.isNumeric() || attribute.isTraceDuration() || attribute.isTraceNumberofEvents()
					|| attribute.isTime()) {
				double[] values = new double[fitness.length];
				int i = 0;
				for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
					IvMTrace trace = it.next();
					double value = getDoubleValue(attribute, trace);
					values[i] = value;
					i++;
				}

				double correlation = Correlation.correlation(fitness, values).doubleValue();
				correlationMap.put(attribute, correlation);

				BufferedImage image = CorrelationDensityPlot.create(fitness, values);
				imageMap.put(attribute, image);
			}
		}
		return Pair.of((TObjectDoubleMap<Attribute>) correlationMap, (Map<Attribute, BufferedImage>) imageMap);
	}

	public static double getDoubleValue(Attribute attribute, IvMTrace trace) {
		double value;
		if (attribute.isNumeric()) {
			value = Attribute.parseDoubleFast(trace.getAttributes().get(attribute.getName()));
		} else if (attribute.isTraceDuration()) {
			value = TraceColourMapPropertyDuration.getTraceDuration(trace);
		} else if (attribute.isTraceNumberofEvents()) {
			value = trace.getNumberOfEvents();
		} else if (attribute.isTime()) {
			value = Attribute.parseTimeFast(trace.getAttributes().get(attribute.getName()));
		} else {
			value = Double.MIN_VALUE;
		}
		return value;
	}

	public static double getDoubleMin(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMin();
		} else {
			return attribute.getTimeMin();
		}
	}

	public static double getDoubleMax(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMax();
		} else {
			return attribute.getTimeMax();
		}
	}

	public double getCorrelation(Attribute attribute) {
		return attribute2correlation.get(attribute);
	}

	public BufferedImage getCorrelationDensityPlot(Attribute attribute) {
		return attribute2correlationDensityPlot.get(attribute);
	}

	public double getCorrelationNegative(Attribute attribute) {
		return attribute2correlationNegative.get(attribute);
	}

	public BufferedImage getCorrelationDensityPlotNegative(Attribute attribute) {
		return attribute2correlationDensityPlotNegative.get(attribute);
	}

	public boolean isSomethingFiltered() {
		return isSomethingFiltered;
	}

}
