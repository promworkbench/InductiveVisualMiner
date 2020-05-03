package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.math.plot.utils.Array;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersLogLogAbstract;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersLogLogDefault;
import org.processmining.earthmoversstochasticconformancechecking.plugins.EarthMoversStochasticConformancePlugin;
import org.processmining.earthmoversstochasticconformancechecking.tracealignments.StochasticTraceAlignmentsLogLog;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis.AttributeData.Field;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog2XLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapPropertyDuration;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import gnu.trove.map.hash.THashMap;

/**
 * Performs the data analysis and stores it.
 * 
 * @author sander
 *
 */
public class TraceAttributeAnalysis {
	public static final int pieSize = 40;

	public static class AttributeData {
		public static enum Field {
			min {
				public String toString() {
					return "minimum";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			average {
				public String toString() {
					return "average";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			median {
				public String toString() {
					return "median";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			max {
				public String toString() {
					return "maximum";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			standardDeviation {
				public String toString() {
					return "standard deviation";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			set {
				public String toString() {
					return "traces with attribute";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			minFitness {
				public String toString() {
					return "minimum fitness";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			averageFitness {
				public String toString() {
					return "average fitness";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			maxFitness {
				public String toString() {
					return "maximum fitness";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			correlation {
				public String toString() {
					return "correlation with fitness";
				}

				public FieldType type() {
					return FieldType.value;
				}
			},
			correlationPlot {
				public String toString() {
					return "correlation with fitness plot";
				}

				public FieldType type() {
					return FieldType.image;
				}
			};

			public abstract FieldType type();
		}

		public enum FieldType {
			value, image
		}

		private DisplayType[] values = new DisplayType[Field.values().length];
		private BufferedImage[] images = new BufferedImage[Field.values().length];

		public AttributeData() {
			Arrays.fill(values, DisplayType.NA());
		}

		public DisplayType getValue(Field field) {
			return values[field.ordinal()];
		}

		public BufferedImage getImage(Field field) {
			return images[field.ordinal()];
		}

		private void set(Field field, DisplayType value) {
			values[field.ordinal()] = value;
		}

		public void set(Field field, BufferedImage image) {
			images[field.ordinal()] = image;
		}
	}

	public static class LogData {
		public int numberOfTraces;
		public double[] fitness;
		public BufferedImage pieSize;
	}

	private Map<Attribute, AttributeData> attribute2data = new THashMap<>();
	private Map<Attribute, AttributeData> attribute2dataNegative = new THashMap<>();

	private LogData logData;
	private LogData logDataNegative;
	private double stochasticSimilarity;
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

		final ConcurrentMap<Attribute, AttributeData> attribute2dataC = new ConcurrentHashMap<>();
		final ConcurrentMap<Attribute, AttributeData> attribute2dataNegativeC = new ConcurrentHashMap<>();
		ExecutorService executor = Executors.newFixedThreadPool(
				Math.max(Runtime.getRuntime().availableProcessors() - 1, 1),
				new ThreadFactoryBuilder().setNameFormat("ivm-thread-tracedataanalysis-%d").build());
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

			//compute stochastic similarity
			if (isSomethingFiltered) {
				executor.execute(new Runnable() {

					public void run() {
						//transform to xlog
						XLog logA = IvMLog2XLog.convert(logFiltered, model);
						XLog logB = IvMLog2XLog.convert(logFilteredNegative, model);

						EMSCParametersLogLogAbstract parameters = new EMSCParametersLogLogDefault();
						parameters.setComputeStochasticTraceAlignments(false);
						try {
							StochasticTraceAlignmentsLogLog alignments = EarthMoversStochasticConformancePlugin
									.measureLogLog(logA, logB, parameters, canceller);
							if (canceller.isCancelled()) {
								return;
							}
							stochasticSimilarity = alignments.getSimilarity();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
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

		//count the number of traces in the unfiltered log
		int unfilteredNumberOfTraces = 0;
		{
			for (IteratorWithPosition<IvMTrace> it = log.iteratorUnfiltered(); it.hasNext();) {
				it.next();
				unfilteredNumberOfTraces++;
			}
		}

		double part = result.numberOfTraces / (unfilteredNumberOfTraces * 1.0);
		if (!isPositive) {
			part = -(1 - part);
		}
		result.pieSize = PieChart.drawPie(pieSize, part, IvMDecorator.textColour);

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

	private AttributeData createAttributeData(IvMLogFiltered logFiltered, LogData logData, Attribute attribute,
			IvMCanceller canceller) {
		AttributeData result = new AttributeData();
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

		result.set(Field.set, DisplayType.numeric(valuesFiltered.length));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			return result;
		}

		result.set(Field.min, DisplayType.create(attributeType, Array.min(valuesFiltered)));

		if (canceller.isCancelled()) {
			return null;
		}

		BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
		result.set(Field.average, DisplayType.create(attributeType, valuesAverage.doubleValue()));

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.median, DisplayType.create(attributeType, Correlation.median(valuesFiltered)));

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.max, DisplayType.create(attributeType, Array.max(valuesFiltered)));

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.minFitness, DisplayType.numeric(Array.min(fitnessFiltered)));

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.averageFitness, DisplayType.numeric(Correlation.mean(fitnessFiltered).doubleValue()));

		if (canceller.isCancelled()) {
			return null;
		}

		result.set(Field.maxFitness, DisplayType.numeric(Array.max(fitnessFiltered)));

		if (canceller.isCancelled()) {
			return null;
		}

		if (result.getValue(Field.min).getValue() != result.getValue(Field.max).getValue()) {
			double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
			{
				if (attribute.isTime()) {
					result.set(Field.standardDeviation, DisplayType.time(Math.round(standardDeviation)));
				} else {
					result.set(Field.standardDeviation, DisplayType.create(attributeType, standardDeviation));
				}
			}

			if (canceller.isCancelled()) {
				return null;
			}

			if (result.getValue(Field.minFitness).getValue() != result.getValue(Field.maxFitness).getValue()) {
				double correlation = Correlation
						.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation).doubleValue();
				if (correlation != -Double.MAX_VALUE) {

					result.set(Field.correlation, DisplayType.numeric(correlation));

					if (canceller.isCancelled()) {
						return null;
					}

					result.set(Field.correlationPlot,
							CorrelationDensityPlot.create(attribute.getName(), valuesFiltered, getDoubleMin(attribute),
									getDoubleMax(attribute), "fitness", fitnessFiltered,
									result.getValue(Field.minFitness).getValue(),
									result.getValue(Field.maxFitness).getValue()));
				}
			}
		}

		return result;
	}

	private static boolean isSupported(Attribute attribute) {
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
			long value = TraceColourMapPropertyDuration.getTraceDuration(trace);
			if (value == Long.MIN_VALUE) {
				return -Double.MAX_VALUE;
			}
			return value;
		} else if (attribute.isTraceNumberofEvents()) {
			return trace.getNumberOfEvents();
		}
		return -Double.MAX_VALUE;
	}

	private static double getDoubleMin(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMin();
		} else {
			return attribute.getTimeMin();
		}
	}

	private static double getDoubleMax(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMax();
		} else {
			return attribute.getTimeMax();
		}
	}

	public AttributeData getAttributeData(Attribute attribute) {
		return attribute2data.get(attribute);
	}

	public AttributeData getAttributeDataNegative(Attribute attribute) {
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

	public double getStochasticSimilarity() {
		return stochasticSimilarity;
	}

	public Collection<Attribute> getTraceAttributes() {
		return attribute2data.keySet();
	}

}
