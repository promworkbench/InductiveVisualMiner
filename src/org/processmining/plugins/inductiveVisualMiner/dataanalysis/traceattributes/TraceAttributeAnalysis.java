package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;
import org.deckfour.xes.model.XAttribute;
import org.math.plot.utils.Array;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Performs the data analysis and stores it.
 * 
 * @author sander
 *
 */
public class TraceAttributeAnalysis {
	public static final int pieSize = 40;

	public static enum Field {
		first {
			public String toString() {
				return "first (alphabetically)";
			}
		},
		last {
			public String toString() {
				return "last (alphabetically)";
			}
		},
		min {
			public String toString() {
				return "minimum";
			}
		},
		average {
			public String toString() {
				return "average";
			}
		},
		median {
			public String toString() {
				return "median";
			}
		},
		max {
			public String toString() {
				return "maximum";
			}
		},
		standardDeviation {
			public String toString() {
				return "standard deviation";
			}
		},
		numberOfDifferentValues {
			public String toString() {
				return "number of distinct values";
			}
		},
		tracesWithAttribute {
			public String toString() {
				return "traces with attribute";
			}
		},
		minFitness {
			public String toString() {
				return "minimum fitness";
			}
		},
		averageFitness {
			public String toString() {
				return "average fitness";
			}
		},
		maxFitness {
			public String toString() {
				return "maximum fitness";
			}
		},
		correlation {
			public String toString() {
				return "correlation with fitness";
			}
		},
		correlationPlot {
			public String toString() {
				return "correlation with fitness plot";
			}
		};
	}

	public enum FieldType {
		value, image
	}

	public static class LogData {
		public int numberOfTraces;
		public double[] fitness;
	}

	private Map<Attribute, EnumMap<Field, DisplayType>> attribute2data = new TreeMap<>(
			EventAttributeAnalysis.attributeNameComparator);
	private Map<Attribute, EnumMap<Field, DisplayType>> attribute2dataNegative = new TreeMap<>(
			EventAttributeAnalysis.attributeNameComparator);

	private LogData logData;
	private LogData logDataNegative;
	private boolean isSomethingFiltered;

	public TraceAttributeAnalysis(final IvMModel model, IvMLogNotFiltered fullLog, final IvMLogFiltered logFiltered,
			AttributesInfo attributes, final IvMCanceller canceller)
			throws CloneNotSupportedException, InterruptedException {
		isSomethingFiltered = logFiltered.isSomethingFiltered();

		final LogData logFilteredData = createLogData(logFiltered, true);
		logData = logFilteredData;

		final IvMLogFilteredImpl logFilteredNegative = logFiltered.clone();
		logFilteredNegative.invert();
		final LogData logFilteredDataNegative = createLogData(logFilteredNegative, false);
		logDataNegative = logFilteredDataNegative;

		final ConcurrentMap<Attribute, EnumMap<Field, DisplayType>> attribute2dataC = new ConcurrentHashMap<>();
		final ConcurrentMap<Attribute, EnumMap<Field, DisplayType>> attribute2dataNegativeC = new ConcurrentHashMap<>();
		ExecutorService executor = Executors.newFixedThreadPool(
				Math.max(Runtime.getRuntime().availableProcessors() - 1, 1),
				new ThreadFactoryBuilder().setNameFormat("ivm-thread-tracedataanalysis-%d").build());
		try {
			for (Attribute attribute : attributes.getTraceAttributes()) {

				final Attribute attribute2 = attribute;
				executor.execute(new Runnable() {
					public void run() {

						if (canceller.isCancelled()) {
							return;
						}

						EnumMap<Field, DisplayType> data = createAttributeData(logFiltered, logFilteredData, attribute2,
								canceller);
						if (data != null) {
							attribute2dataC.put(attribute2, data);
						}
					}
				});

				if (isSomethingFiltered) {
					executor.execute(new Runnable() {
						public void run() {

							if (canceller.isCancelled()) {
								return;
							}

							EnumMap<Field, DisplayType> dataNegative = createAttributeData(logFilteredNegative,
									logFilteredDataNegative, attribute2, canceller);
							if (dataNegative != null) {
								attribute2dataNegativeC.put(attribute2, dataNegative);
							}
						}
					});
				}
			}

			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} finally {
			executor.shutdownNow();
		}

		for (Entry<Attribute, EnumMap<Field, DisplayType>> e : attribute2dataC.entrySet()) {
			attribute2data.put(e.getKey(), e.getValue());
		}
		for (Entry<Attribute, EnumMap<Field, DisplayType>> e : attribute2dataNegativeC.entrySet()) {
			attribute2dataNegative.put(e.getKey(), e.getValue());
		}
	}

	private LogData createLogData(IvMLogFiltered log, boolean isPositive) {
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

	private EnumMap<Field, DisplayType> createAttributeData(IvMLogFiltered logFiltered, LogData logData,
			Attribute attribute, IvMCanceller canceller) {
		EnumMap<Field, DisplayType> result = new EnumMap<>(Field.class);
		if (attribute.isNumeric()) {
			createAttributeDataNumeric(result, logFiltered, logData, attribute, canceller);
		} else if (attribute.isTime()) {
			createAttributeDataTime(result, logFiltered, logData, attribute, canceller);
		} else if (attribute.isLiteral()) {
			createAttributeDataLiteral(result, logFiltered, logData, attribute, canceller);
			//TODO: update
//		} else if (attribute.isDuration()) {
//			createAttributeDataDuration(result, logFiltered, logData, attribute, canceller);
		}

		if (canceller.isCancelled()) {
			return null;
		}

		return result;
	}

	private void createAttributeDataNumeric(EnumMap<Field, DisplayType> result, IvMLogFiltered logFiltered,
			LogData logData, Attribute attribute, IvMCanceller canceller) {
		Type attributeType = DisplayType.fromAttribute(attribute);

		//compute correlation and plots
		double[] fitnessFiltered;
		double[] valuesFiltered;
		{
			double[] values = new double[logData.numberOfTraces];
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				double value = getDoubleValue(attribute, trace);

				//store the value
				values[i] = value;

				i++;
			}

			if (canceller.isCancelled()) {
				return;
			}

			//filter missing values
			Pair<double[], double[]> p = Correlation.filterMissingValues(logData.fitness, values);
			fitnessFiltered = p.getA();
			valuesFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return;
		}

		result.put(Field.tracesWithAttribute, DisplayType.numeric(valuesFiltered.length));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			result.put(Field.min, DisplayType.NA());
			result.put(Field.average, DisplayType.NA());
			result.put(Field.median, DisplayType.NA());
			result.put(Field.max, DisplayType.NA());
			result.put(Field.minFitness, DisplayType.NA());
			result.put(Field.averageFitness, DisplayType.NA());
			result.put(Field.maxFitness, DisplayType.NA());
			result.put(Field.standardDeviation, DisplayType.NA());
			result.put(Field.correlation, DisplayType.NA());
			result.put(Field.correlationPlot, DisplayType.NA());
		} else {

			result.put(Field.min, DisplayType.create(attributeType, Array.min(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
			result.put(Field.average, DisplayType.create(attributeType, valuesAverage.doubleValue()));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.median, DisplayType.create(attributeType, Correlation.median(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.max, DisplayType.create(attributeType, Array.max(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.minFitness, DisplayType.numeric(Array.min(fitnessFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.averageFitness, DisplayType.numeric(Correlation.mean(fitnessFiltered).doubleValue()));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.maxFitness, DisplayType.numeric(Array.max(fitnessFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			if (result.get(Field.min).getValue() == result.get(Field.max).getValue()) {
				result.put(Field.standardDeviation, DisplayType.NA());
				result.put(Field.correlation, DisplayType.NA());
				result.put(Field.correlationPlot, DisplayType.NA());
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.put(Field.standardDeviation, DisplayType.create(attributeType, standardDeviation));

				if (canceller.isCancelled()) {
					return;
				}

				if (result.get(Field.minFitness).getValue() == result.get(Field.maxFitness).getValue()) {
					result.put(Field.correlation, DisplayType.NA());
					result.put(Field.correlationPlot, DisplayType.NA());
				} else {
					double correlation = Correlation
							.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation)
							.doubleValue();
					if (correlation == -Double.MAX_VALUE) {
						result.put(Field.correlation, DisplayType.NA());
						result.put(Field.correlationPlot, DisplayType.NA());
					} else {
						result.put(Field.correlation, DisplayType.numeric(correlation));

						if (canceller.isCancelled()) {
							return;
						}

						BufferedImage plot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered,
								getDoubleMin(attribute), getDoubleMax(attribute), "fitness", fitnessFiltered,
								result.get(Field.minFitness).getValue(), result.get(Field.maxFitness).getValue());
						result.put(Field.correlationPlot, DisplayType.image(plot));
					}
				}
			}
		}
	}

	private void createAttributeDataTime(EnumMap<Field, DisplayType> result, IvMLogFiltered logFiltered,
			LogData logData, Attribute attribute, IvMCanceller canceller) {
		Type attributeType = Type.time;

		//compute correlation and plots
		double[] fitnessFiltered;
		long[] valuesFiltered;
		{
			long[] values = new long[logData.numberOfTraces];
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				long value = getLongValue(attribute, trace);

				//store the value
				values[i] = value;

				i++;
			}

			if (canceller.isCancelled()) {
				return;
			}

			//filter missing values
			Pair<long[], double[]> p = Correlation.filterMissingValues(values, logData.fitness);
			valuesFiltered = p.getA();
			fitnessFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return;
		}

		result.put(Field.tracesWithAttribute, DisplayType.numeric(valuesFiltered.length));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			result.put(Field.min, DisplayType.NA());
			result.put(Field.average, DisplayType.NA());
			result.put(Field.median, DisplayType.NA());
			result.put(Field.max, DisplayType.NA());
			result.put(Field.minFitness, DisplayType.NA());
			result.put(Field.averageFitness, DisplayType.NA());
			result.put(Field.maxFitness, DisplayType.NA());
			result.put(Field.standardDeviation, DisplayType.NA());
			result.put(Field.correlation, DisplayType.NA());
			result.put(Field.correlationPlot, DisplayType.NA());
		} else {
			result.put(Field.min, DisplayType.create(attributeType, NumberUtils.min(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
			result.put(Field.average, DisplayType.create(attributeType, valuesAverage.longValue()));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.median, DisplayType.create(attributeType, Math.round(Correlation.median(valuesFiltered))));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.max, DisplayType.create(attributeType, NumberUtils.max(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.minFitness, DisplayType.numeric(Array.min(fitnessFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.averageFitness, DisplayType.numeric(Correlation.mean(fitnessFiltered).doubleValue()));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.maxFitness, DisplayType.numeric(Array.max(fitnessFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			if (result.get(Field.min).getValue() == result.get(Field.max).getValue()) {
				result.put(Field.standardDeviation, DisplayType.NA());
				result.put(Field.correlation, DisplayType.NA());
				result.put(Field.correlationPlot, DisplayType.NA());
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.put(Field.standardDeviation, DisplayType.create(Type.duration, standardDeviation));

				if (canceller.isCancelled()) {
					return;
				}

				if (result.get(Field.minFitness).getValue() == result.get(Field.maxFitness).getValue()) {
					result.put(Field.correlation, DisplayType.NA());
					result.put(Field.correlationPlot, DisplayType.NA());
				} else {
					double correlation = Correlation
							.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation)
							.doubleValue();
					if (correlation == -Double.MAX_VALUE) {
						result.put(Field.correlation, DisplayType.NA());
						result.put(Field.correlationPlot, DisplayType.NA());
					} else {
						result.put(Field.correlation, DisplayType.numeric(correlation));

						if (canceller.isCancelled()) {
							return;
						}

						BufferedImage plot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered,
								((DisplayType.Time) result.get(Field.min)).getValueLong(),
								((DisplayType.Time) result.get(Field.max)).getValueLong(), "fitness", fitnessFiltered,
								result.get(Field.minFitness).getValue(), result.get(Field.maxFitness).getValue());
						result.put(Field.correlationPlot, DisplayType.image(plot));
					}
				}
			}
		}
	}

	private void createAttributeDataLiteral(EnumMap<Field, DisplayType> result, IvMLogFiltered logFiltered,
			LogData logData, Attribute attribute, IvMCanceller canceller) {
		int numberOfTracesWithAttribute = 0;
		{
			for (IteratorWithPosition<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();

				if (trace.getAttributes().containsKey(attribute.getName())) {
					numberOfTracesWithAttribute++;
				}
			}
		}

		if (canceller.isCancelled()) {
			return;
		}

		result.put(Field.tracesWithAttribute, DisplayType.numeric(numberOfTracesWithAttribute));

		ArrayList<String> valueSet = new ArrayList<>(attribute.getStringValues());
		result.put(Field.numberOfDifferentValues, DisplayType.numeric(valueSet.size()));

		if (valueSet.isEmpty()) {
			result.put(Field.first, DisplayType.NA());
			result.put(Field.last, DisplayType.NA());
		} else {
			int first = 0;
			int last = 0;
			for (int i = 1; i < valueSet.size(); i++) {
				if (valueSet.get(first).toLowerCase().compareTo(valueSet.get(i).toLowerCase()) > 0) {
					first = i;
				} else if (valueSet.get(last).toLowerCase().compareTo(valueSet.get(i).toLowerCase()) < 0) {
					last = i;
				}
			}
			result.put(Field.first, DisplayType.literal(valueSet.get(first)));
			result.put(Field.last, DisplayType.literal(valueSet.get(last)));
		}
	}

	private void createAttributeDataDuration(EnumMap<Field, DisplayType> result, IvMLogFiltered logFiltered,
			LogData logData, Attribute attribute, IvMCanceller canceller) {
		Type attributeType = Type.duration;

		//compute correlation and plots
		double[] fitnessFiltered;
		long[] valuesFiltered;
		{
			long[] values = new long[logData.numberOfTraces];
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				long value = getLongValue(attribute, trace);

				//store the value
				values[i] = value;

				i++;
			}

			if (canceller.isCancelled()) {
				return;
			}

			//filter missing values
			Pair<long[], double[]> p = Correlation.filterMissingValues(values, logData.fitness);
			valuesFiltered = p.getA();
			fitnessFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return;
		}

		result.put(Field.tracesWithAttribute, DisplayType.numeric(valuesFiltered.length));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			result.put(Field.min, DisplayType.NA());
			result.put(Field.average, DisplayType.NA());
			result.put(Field.median, DisplayType.NA());
			result.put(Field.max, DisplayType.NA());
			result.put(Field.minFitness, DisplayType.NA());
			result.put(Field.averageFitness, DisplayType.NA());
			result.put(Field.maxFitness, DisplayType.NA());
			result.put(Field.standardDeviation, DisplayType.NA());
			result.put(Field.correlation, DisplayType.NA());
			result.put(Field.correlationPlot, DisplayType.NA());
		} else {
			result.put(Field.min, DisplayType.create(attributeType, NumberUtils.min(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
			result.put(Field.average, DisplayType.create(attributeType, valuesAverage.longValue()));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.median, DisplayType.create(attributeType, Math.round(Correlation.median(valuesFiltered))));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.max, DisplayType.create(attributeType, NumberUtils.max(valuesFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.minFitness, DisplayType.numeric(Array.min(fitnessFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.averageFitness, DisplayType.numeric(Correlation.mean(fitnessFiltered).doubleValue()));

			if (canceller.isCancelled()) {
				return;
			}

			result.put(Field.maxFitness, DisplayType.numeric(Array.max(fitnessFiltered)));

			if (canceller.isCancelled()) {
				return;
			}

			if (result.get(Field.min).getValue() == result.get(Field.max).getValue()) {
				result.put(Field.standardDeviation, DisplayType.NA());
				result.put(Field.correlation, DisplayType.NA());
				result.put(Field.correlationPlot, DisplayType.NA());
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.put(Field.standardDeviation, DisplayType.create(attributeType, standardDeviation));

				if (canceller.isCancelled()) {
					return;
				}

				if (result.get(Field.minFitness).getValue() == result.get(Field.maxFitness).getValue()) {
					result.put(Field.correlation, DisplayType.NA());
					result.put(Field.correlationPlot, DisplayType.NA());
				} else {
					double correlation = Correlation
							.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation)
							.doubleValue();
					if (correlation == -Double.MAX_VALUE) {
						result.put(Field.correlation, DisplayType.NA());
						result.put(Field.correlationPlot, DisplayType.NA());
					} else {
						result.put(Field.correlation, DisplayType.numeric(correlation));

						if (canceller.isCancelled()) {
							return;
						}

						BufferedImage plot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered,
								((DisplayType.Time) result.get(Field.min)).getValueLong(),
								((DisplayType.Time) result.get(Field.max)).getValueLong(), "fitness", fitnessFiltered,
								result.get(Field.minFitness).getValue(), result.get(Field.maxFitness).getValue());
						result.put(Field.correlationPlot, DisplayType.image(plot));
					}
				}
			}
		}
	}

	public static double getDoubleValue(Attribute attribute, IvMTrace trace) {
		if (attribute.isVirtual()) {
			//TODO: ?
		} else if (attribute.isNumeric()) {
			XAttribute xAttribute = trace.getAttributes().get(attribute.getName());
			if (xAttribute == null) {
				return -Double.MAX_VALUE;
			}
			return AttributeUtils.parseDoubleFast(xAttribute);
		} else if (attribute.isTime()) {
			XAttribute xAttribute = trace.getAttributes().get(attribute.getName());
			if (xAttribute == null) {
				return -Double.MAX_VALUE;
			}
			return AttributeUtils.parseTimeFast(xAttribute);
		}
		//		if (attribute.isTraceDuration()) {
		//			long value = TraceColourMapPropertyDuration.getTraceDuration(trace);
		//			if (value == Long.MIN_VALUE) {
		//				return -Double.MAX_VALUE;
		//			}
		//			return value;
		//		} else if (attribute.isTraceNumberofEvents()) {
		//			return trace.getNumberOfEvents();
		//		}
		return -Double.MAX_VALUE;
	}

	public static long getLongValue(Attribute attribute, IvMTrace trace) {
		if (attribute.isVirtual()) {
			//TODO: ?
		} else if (attribute.isNumeric()) {
			XAttribute xAttribute = trace.getAttributes().get(attribute.getName());
			if (xAttribute == null) {
				return Long.MIN_VALUE;
			}
			return AttributeUtils.parseLongFast(xAttribute);
		} else if (attribute.isTime()) {
			XAttribute xAttribute = trace.getAttributes().get(attribute.getName());
			if (xAttribute == null) {
				return Long.MIN_VALUE;
			}
			return AttributeUtils.parseTimeFast(xAttribute);
			//		} else if (attribute.isTraceDuration()) {
			//			return TraceColourMapPropertyDuration.getTraceDuration(trace);
			//		} else if (attribute.isTraceNumberofEvents()) {
			//			return trace.getNumberOfEvents();
		}
		return Long.MIN_VALUE;
	}

	private static double getDoubleMin(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMin();
			//TODO: update
//		} else if (attribute.isDuration()) {
//			return attribute.getDurationMin();
		} else {
			return attribute.getTimeMin();
		}
	}

	private static double getDoubleMax(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMax();
			//TODO: update
//		} else if (attribute.isDuration()) {
//			return attribute.getDurationMax();
		} else {
			return attribute.getTimeMax();
		}
	}

	public EnumMap<Field, DisplayType> getAttributeData(Attribute attribute) {
		return attribute2data.get(attribute);
	}

	public EnumMap<Field, DisplayType> getAttributeDataNegative(Attribute attribute) {
		return attribute2dataNegative.get(attribute);
	}

	public LogData getLogData() {
		return logData;
	}

	public LogData getLogDataNegative() {
		return logDataNegative;
	}

	public boolean isSomethingFiltered() {
		return isSomethingFiltered;
	}

	public Collection<Attribute> getTraceAttributes() {
		return attribute2data.keySet();
	}

}
