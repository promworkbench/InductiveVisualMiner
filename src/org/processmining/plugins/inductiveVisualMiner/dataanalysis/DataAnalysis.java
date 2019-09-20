package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.model.XAttribute;
import org.math.plot.utils.Array;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData.Field;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapPropertyDuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
			set {
				public String toString() {
					return "traces with attribute";
				}

				public boolean forcedNumeric() {
					return true;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			min {
				public String toString() {
					return "minimum";
				}

				public boolean forcedNumeric() {
					return false;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			average {
				public String toString() {
					return "average";
				}

				public boolean forcedNumeric() {
					return false;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			max {
				public String toString() {
					return "maximum";
				}

				public boolean forcedNumeric() {
					return false;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			median {
				public String toString() {
					return "median";
				}

				public boolean forcedNumeric() {
					return false;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			standardDeviation {
				public String toString() {
					return "standard deviation";
				}

				public boolean forcedNumeric() {
					return false;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			correlation {
				public String toString() {
					return "correlation with fitness";
				}

				public boolean forcedNumeric() {
					return true;
				}

				public FieldType type() {
					return FieldType.number;
				}
			},
			correlationPlot {
				public String toString() {
					return "correlation plot";
				}

				public boolean forcedNumeric() {
					return false;
				}

				public FieldType type() {
					return FieldType.image;
				}
			};

			public abstract boolean forcedNumeric();

			public abstract FieldType type();
		}

		public enum FieldType {
			number, image
		}

		private double[] values = new double[Field.values().length];
		private BufferedImage[] images = new BufferedImage[Field.values().length];

		public AttributeData() {
			Arrays.fill(values, -Double.MAX_VALUE);
		}

		public double getNumber(Field field) {
			return values[field.ordinal()];
		}

		public BufferedImage getImage(Field field) {
			return images[field.ordinal()];
		}

		private void set(Field field, double value) {
			values[field.ordinal()] = value;
		}

		public void set(Field field, BufferedImage image) {
			images[field.ordinal()] = image;
		}
	}

	private static class LogData {
		public int numberOfTraces;
		double[] fitness;
	}

	Map<Attribute, AttributeData> attribute2data = new THashMap<>();
	Map<Attribute, AttributeData> attribute2dataNegative = new THashMap<>();

	private double globalMinFitness;
	private double globalMaxFitness;
	private boolean isSomethingFiltered;

	public DataAnalysis(IvMLogNotFiltered fullLog, final IvMLogFiltered logFiltered, AttributesInfo attributes,
			final IvMCanceller canceller) throws CloneNotSupportedException, InterruptedException {
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

		if (canceller.isCancelled()) {
			return;
		}

		isSomethingFiltered = logFiltered.isSomethingFiltered();

		final LogData logFilteredData = createLogData(logFiltered);

		final IvMLogFilteredImpl logFilteredNegative = logFiltered.clone();
		logFilteredNegative.invert();
		final LogData logFilteredDataNegative = createLogData(logFilteredNegative);

		final ConcurrentMap<Attribute, AttributeData> attribute2dataC = new ConcurrentHashMap<>();
		final ConcurrentMap<Attribute, AttributeData> attribute2dataNegativeC = new ConcurrentHashMap<>();
		ExecutorService executor = Executors.newFixedThreadPool(
				Math.max(Runtime.getRuntime().availableProcessors() - 1, 1),
				new ThreadFactoryBuilder().setNameFormat("ivm-thread-dataanalysis-%d").build());
		try {

			for (Attribute attribute : attributes.getTraceAttributes()) {
				if (isSupported(attribute)) {
					final Attribute attribute2 = attribute;
					executor.execute(new Runnable() {
						public void run() {

							if (canceller.isCancelled()) {
								return;
							}

							AttributeData data = createAttributeData(logFiltered, logFilteredData, attribute2,
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

								AttributeData dataNegative = createAttributeData(logFilteredNegative,
										logFilteredDataNegative, attribute2, canceller);
								if (dataNegative != null) {
									attribute2dataNegativeC.put(attribute2, dataNegative);
								}
							}
						});
					}
				}
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} finally {
			executor.shutdownNow();
		}

		for (Entry<Attribute, AttributeData> e : attribute2dataC.entrySet()) {
			attribute2data.put(e.getKey(), e.getValue());
		}
		for (Entry<Attribute, AttributeData> e : attribute2dataNegativeC.entrySet()) {
			attribute2dataNegative.put(e.getKey(), e.getValue());
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

	public AttributeData createAttributeData(IvMLogFiltered logFiltered, LogData logData, Attribute attribute,
			IvMCanceller canceller) {
		AttributeData result = new AttributeData();

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
				return null;
			}

			//filter missing values
			Pair<double[], double[]> p = Correlation.filterMissingValues(logData.fitness, values);
			fitnessFiltered = p.getA();
			valuesFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.set, valuesFiltered.length);

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			return result;
		}

		result.set(Field.min, Array.min(valuesFiltered));

		if (canceller.isCancelled()) {
			return null;
		}

		BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
		result.set(Field.average, valuesAverage.doubleValue());

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.median, Correlation.median(valuesFiltered));

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.max, Array.max(valuesFiltered));

		if (canceller.isCancelled()) {
			return null;
		}

		double valuesStandardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
		result.set(Field.standardDeviation, valuesStandardDeviation);

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.correlation, Correlation
				.correlation(fitnessFiltered, valuesFiltered, valuesAverage, valuesStandardDeviation).doubleValue());

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.correlationPlot,
				CorrelationDensityPlot.create(attribute.getName(), valuesFiltered, getDoubleMin(attribute),
						getDoubleMax(attribute), "fitness", fitnessFiltered, globalMinFitness, globalMaxFitness));

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
