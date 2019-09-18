package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData.Field;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapPropertyDuration;

import gnu.trove.map.hash.THashMap;

/**
 * Performs the data analysis and stores it.
 * 
 * @author sander
 *
 */
public class DataAnalysis {
	public static final DecimalFormat numberFormat = new DecimalFormat("#.####");

	public static class AttributeData {
		public static enum Field {
			min {
				public String toString() {
					return "minimum";
				}
			},
			max {
				public String toString() {
					return "maximum";
				}
			},
			correlation {
				public String toString() {
					return "correlation with fitness";
				}
			},
		}

		private double values[] = new double[Field.values().length];

		private BufferedImage correlationDensityPlot;

		public double get(Field field) {
			return values[field.ordinal()];
		}

		private void set(Field field, double value) {
			values[field.ordinal()] = value;
		}

		public BufferedImage getCorrelationDensityPlot() {
			return correlationDensityPlot;
		}
	}

	private static class LogData {
		public int numberOfTraces;
		double[] fitness;
	}

	Map<Attribute, AttributeData> attribute2data = new THashMap<>();
	Map<Attribute, AttributeData> attribute2dataNegative = new THashMap<>();

	private final double globalMinFitness;
	private final double globalMaxFitness;
	private final boolean isSomethingFiltered;

	public DataAnalysis(IvMLogNotFiltered fullLog, IvMLogFiltered logFiltered, AttributesInfo attributes)
			throws CloneNotSupportedException {
		//compute global min and max fitness
		{
			double min = 1;
			double max = 0;
			for (Iterator<IvMTrace> it = fullLog.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();

				double fitness = Fitness.compute(trace);
				min = Math.min(min, fitness);
				max = Math.max(max, fitness);
			}
			globalMinFitness = min;
			globalMaxFitness = max;
		}

		isSomethingFiltered = logFiltered.isSomethingFiltered();

		LogData logFilteredData = createLogData(logFiltered);

		IvMLogFilteredImpl logFilteredNegative = logFiltered.clone();
		logFilteredNegative.invert();
		LogData logFilteredDataNegative = createLogData(logFilteredNegative);

		for (Attribute attribute : attributes.getTraceAttributes()) {
			if (isSupported(attribute)) {
				AttributeData data = createAttributeData(logFiltered, logFilteredData, attribute);
				attribute2data.put(attribute, data);

				if (isSomethingFiltered) {
					AttributeData dataNegative = createAttributeData(logFilteredNegative, logFilteredDataNegative,
							attribute);
					attribute2dataNegative.put(attribute, dataNegative);
				}
			}
		}
	}

	public LogData createLogData(IvMLogFiltered log) {
		LogData result = new LogData();

		//count the number of traces
		result.numberOfTraces = 0;
		{
			for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
				it.next();
				result.numberOfTraces++;
			}
		}

		//compute fitness
		result.fitness = new double[result.numberOfTraces];
		{
			int i = 0;
			for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();

				result.fitness[i] = Fitness.compute(trace);
				i++;
			}
		}

		return result;
	}

	public AttributeData createAttributeData(IvMLogFiltered logFiltered, LogData logData, Attribute attribute) {
		AttributeData result = new AttributeData();

		//compute correlation and plots
		double[] values = new double[logData.numberOfTraces];
		int i = 0;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			double value = getDoubleValue(attribute, trace);

			//keep track of min and max
			if (value > -Double.MAX_VALUE) {
				min = Math.min(min, value);
				max = Math.max(max, value);
			}

			//store the value
			values[i] = value;

			i++;
		}

		result.set(Field.min, min);
		result.set(Field.max, max);

		//filter missing values
		Pair<double[], double[]> p = Correlation.filterMissingValues(logData.fitness, values);
		double[] fitnessFiltered = p.getA();
		double[] valuesFiltered = p.getB();

		result.set(Field.correlation, Correlation.correlation(fitnessFiltered, valuesFiltered));

		result.correlationDensityPlot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered,
				getDoubleMin(attribute), getDoubleMax(attribute), "fitness", fitnessFiltered, globalMinFitness,
				globalMaxFitness);

		return result;
	}

	public static boolean isSupported(Attribute attribute) {
		return attribute.isNumeric() || attribute.isTime() || attribute.isTraceDuration()
				|| attribute.isTraceNumberofEvents();
	}

	public static double getDoubleValue(Attribute attribute, IvMTrace trace) {
		if (attribute.isNumeric() || attribute.isTime()) {
			XAttribute xAttribute = trace.getAttributes().get(attribute.getName());
			if (xAttribute == null) {
				return -Double.MAX_VALUE;
			}
			if (attribute.isNumeric()) {
				return Attribute.parseDoubleFast(xAttribute);
			} else if (attribute.isTime()) {
				return Attribute.parseTimeFast(xAttribute);
			}
		}
		if (attribute.isTraceDuration()) {
			return TraceColourMapPropertyDuration.getTraceDuration(trace);
		} else if (attribute.isTraceNumberofEvents()) {
			return trace.getNumberOfEvents();
		}
		return -Double.MAX_VALUE;
	}

	public static double getDoubleMin(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMin();
		} else {
			return attribute.getTimeMin();
		}
	}

	public static String getStringMin(Attribute attribute) {
		if (attribute.isNumeric()) {
			return numberFormat.format(attribute.getNumericMin());
		} else if (attribute.isTraceDuration()) {
			return ResourceTimeUtils.getDuration(attribute.getTimeMin());
		} else if (attribute.isTraceNumberofEvents()) {
			return attribute.getTimeMin() + "";
		} else {
			return ResourceTimeUtils.timeToString(attribute.getTimeMin());
		}
	}

	public static double getDoubleMax(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMax();
		} else {
			return attribute.getTimeMax();
		}
	}

	public static String getStringMax(Attribute attribute) {
		if (attribute.isNumeric()) {
			return numberFormat.format(attribute.getNumericMax());
		} else if (attribute.isTraceDuration()) {
			return ResourceTimeUtils.getDuration(attribute.getTimeMax());
		} else if (attribute.isTraceNumberofEvents()) {
			return attribute.getTimeMax() + "";
		} else {
			return ResourceTimeUtils.timeToString(attribute.getTimeMax());
		}
	}

	public static String getString(Attribute attribute, double value) {
		if (attribute.isNumeric()) {
			return numberFormat.format(value);
		} else if (attribute.isTraceDuration()) {
			return ResourceTimeUtils.getDuration(value);
		} else if (attribute.isTraceNumberofEvents()) {
			return ((long) value) + "";
		} else {
			return ResourceTimeUtils.timeToString((long) value);
		}
	}

	public AttributeData getAttributeData(Attribute attribute) {
		return attribute2data.get(attribute);
	}

	public AttributeData getAttributeDataNegative(Attribute attribute) {
		return attribute2dataNegative.get(attribute);
	}

	public boolean isSomethingFiltered() {
		return isSomethingFiltered;
	}

}
