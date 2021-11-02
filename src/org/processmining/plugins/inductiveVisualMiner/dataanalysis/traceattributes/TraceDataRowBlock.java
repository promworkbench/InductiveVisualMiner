package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.math.plot.utils.Array;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class TraceDataRowBlock<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "trace-att";
	}

	public String getStatusBusyMessage() {
		return "Gathering trace attributes..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.attributes_info, IvMObject.aligned_log_filtered,
				IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		AttributesInfo attributes = inputs.get(IvMObject.attributes_info);

		//count number of traces
		int numberOfTraces = getNumberOfTraces(logFiltered);
		double[] trace2fitness = getTrace2fitness(logFiltered, numberOfTraces);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();
			int numberOfTracesNegative = getNumberOfTraces(negativeLog);
			double[] trace2fitnessNegative = getTrace2fitness(negativeLog, numberOfTracesNegative);

			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(
						merge(createAttributeData(logFiltered, attribute, numberOfTraces, trace2fitness, canceller),
								createAttributeData(negativeLog, attribute, numberOfTracesNegative,
										trace2fitnessNegative, canceller),
								canceller));
			}
		} else {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(createAttributeData(logFiltered, attribute, numberOfTraces, trace2fitness, canceller));
			}
		}

		return result;
	}

	public static int getNumberOfTraces(IvMLogFiltered log) {
		int numberOfTraces = 0;
		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			it.next();
			numberOfTraces++;
		}
		return numberOfTraces;
	}

	public static double[] getTrace2fitness(IvMLogFiltered log, int numberOfTraces) {
		//compute fitness
		double[] result = new double[numberOfTraces];
		int i = 0;
		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			result[i] = Fitness.compute(trace);
			i++;
		}
		return result;
	}

	/**
	 * Merges a datarow from each if their names match. Assumption: each input
	 * datarow has only one value. In-place.
	 * 
	 * @param <O>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <O> List<DataRow<O>> merge(List<DataRow<O>> a, List<DataRow<O>> b, IvMCanceller canceller) {
		if (a == null || b == null) {
			return null;
		}
		for (DataRow<O> dataRowA : a) {

			if (canceller != null && canceller.isCancelled()) {
				return a;
			}

			DataRow<O> dataRowB = null;
			for (DataRow<O> drB : b) {
				if (Arrays.equals(dataRowA.getNames(), drB.getNames())) {
					dataRowB = drB;
					break;
				}
			}

			if (dataRowB == null) {
				dataRowA.setValues(dataRowA.getValue(0), DisplayType.NA());
			} else {
				dataRowA.setValues(dataRowA.getValue(0), dataRowB.getValue(0));
			}
		}
		return a;
	}

	public static List<DataRow<Object>> createAttributeData(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, double[] trace2fitness, IvMCanceller canceller) {
		if (attribute.isNumeric()) {
			return createAttributeDataNumeric(logFiltered, attribute, numberOfTraces, trace2fitness, canceller);
		} else if (attribute.isTime()) {
			return createAttributeDataTime(logFiltered, attribute, numberOfTraces, trace2fitness, canceller);
		} else if (attribute.isLiteral()) {
			return createAttributeDataLiteral(logFiltered, attribute, numberOfTraces, trace2fitness, canceller);
		} else if (attribute.isDuration()) {
			return createAttributeDataDuration(logFiltered, attribute, numberOfTraces, trace2fitness, canceller);
		}

		List<DataRow<Object>> result = new ArrayList<>();
		result.add(new DataRow<Object>(DisplayType.literal("[not supported]"), attribute.getName(), ""));
		return result;
	}

	private static List<DataRow<Object>> createAttributeDataNumeric(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, double[] trace2fitness, IvMCanceller canceller) {
		Type attributeType = DisplayType.fromAttribute(attribute);

		List<DataRow<Object>> result = new ArrayList<>();

		//compute correlation and plots
		double[] fitnessFiltered;
		double[] valuesFiltered;
		{
			double[] values = new double[numberOfTraces];
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				double value = AttributeUtils.valueDouble(attribute, trace);

				//store the value
				values[i] = value;

				i++;
			}

			if (canceller.isCancelled()) {
				return result;
			}

			//filter missing values
			Pair<double[], double[]> p = Correlation.filterMissingValues(trace2fitness, values);
			fitnessFiltered = p.getA();
			valuesFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return result;
		}

		result.add(c(attribute, Field.tracesWithAttribute, DisplayType.numeric(valuesFiltered.length)));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			result.add(c(attribute, Field.min, DisplayType.NA()));
			result.add(c(attribute, Field.average, DisplayType.NA()));
			result.add(c(attribute, Field.median, DisplayType.NA()));
			result.add(c(attribute, Field.max, DisplayType.NA()));
			result.add(c(attribute, Field.minFitness, DisplayType.NA()));
			result.add(c(attribute, Field.averageFitness, DisplayType.NA()));
			result.add(c(attribute, Field.maxFitness, DisplayType.NA()));
			result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
			result.add(c(attribute, Field.correlation, DisplayType.NA()));
			result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
		} else {
			double min = Array.min(valuesFiltered);
			result.add(c(attribute, Field.min, DisplayType.create(attributeType, min)));

			if (canceller.isCancelled()) {
				return result;
			}

			BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
			assert valuesAverage != null;
			result.add(c(attribute, Field.average, DisplayType.create(attributeType, valuesAverage.doubleValue())));

			if (canceller.isCancelled()) {
				return result;
			}

			result.add(
					c(attribute, Field.median, DisplayType.create(attributeType, Correlation.median(valuesFiltered))));

			if (canceller.isCancelled()) {
				return result;
			}

			double max = Array.max(valuesFiltered);
			result.add(c(attribute, Field.max, DisplayType.create(attributeType, max)));

			if (canceller.isCancelled()) {
				return result;
			}

			double minFitness = Array.min(fitnessFiltered);
			result.add(c(attribute, Field.minFitness, DisplayType.numeric(minFitness)));

			if (canceller.isCancelled()) {
				return result;
			}

			result.add(c(attribute, Field.averageFitness,
					DisplayType.numeric(Correlation.mean(fitnessFiltered).doubleValue())));

			if (canceller.isCancelled()) {
				return result;
			}

			double maxFitness = Array.max(fitnessFiltered);
			result.add(c(attribute, Field.maxFitness, DisplayType.numeric(maxFitness)));

			if (canceller.isCancelled()) {
				return result;
			}

			if (min == max) {
				result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
				result.add(c(attribute, Field.correlation, DisplayType.NA()));
				result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.add(c(attribute, Field.standardDeviation, DisplayType.create(attributeType, standardDeviation)));

				if (canceller.isCancelled()) {
					return result;
				}

				if (minFitness == maxFitness) {
					result.add(c(attribute, Field.correlation, DisplayType.NA()));
					result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
				} else {
					double correlation = Correlation
							.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation)
							.doubleValue();
					if (correlation == -Double.MAX_VALUE) {
						result.add(c(attribute, Field.correlation, DisplayType.NA()));
						result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
					} else {
						result.add(c(attribute, Field.correlation, DisplayType.numeric(correlation)));

						if (canceller.isCancelled()) {
							return result;
						}

						BufferedImage plot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered,
								getDoubleMin(attribute), getDoubleMax(attribute), "fitness", fitnessFiltered,
								minFitness, maxFitness);
						result.add(c(attribute, Field.correlationPlot, DisplayType.image(plot)));
					}
				}
			}
		}

		return result;
	}

	private static List<DataRow<Object>> createAttributeDataTime(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, double[] trace2fitness, IvMCanceller canceller) {
		Type attributeType = Type.time;

		List<DataRow<Object>> result = new ArrayList<>();

		//compute correlation and plots
		double[] fitnessFiltered;
		long[] valuesFiltered;
		{
			long[] values = new long[numberOfTraces];
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				long value = AttributeUtils.valueLong(attribute, trace);

				//store the value
				values[i] = value;

				i++;
			}

			if (canceller.isCancelled()) {
				return result;
			}

			//filter missing values
			Pair<long[], double[]> p = Correlation.filterMissingValues(values, trace2fitness);
			valuesFiltered = p.getA();
			fitnessFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return result;
		}

		result.add(c(attribute, Field.tracesWithAttribute, DisplayType.numeric(valuesFiltered.length)));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			result.add(c(attribute, Field.min, DisplayType.NA()));
			result.add(c(attribute, Field.average, DisplayType.NA()));
			result.add(c(attribute, Field.median, DisplayType.NA()));
			result.add(c(attribute, Field.max, DisplayType.NA()));
			result.add(c(attribute, Field.minFitness, DisplayType.NA()));
			result.add(c(attribute, Field.averageFitness, DisplayType.NA()));
			result.add(c(attribute, Field.maxFitness, DisplayType.NA()));
			result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
			result.add(c(attribute, Field.correlation, DisplayType.NA()));
			result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
		} else {
			long min = NumberUtils.min(valuesFiltered);
			result.add(c(attribute, Field.min, DisplayType.create(attributeType, min)));

			if (canceller.isCancelled()) {
				return result;
			}

			BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
			result.add(c(attribute, Field.average, DisplayType.create(attributeType, valuesAverage.longValue())));

			if (canceller.isCancelled()) {
				return result;
			}

			result.add(c(attribute, Field.median,
					DisplayType.create(attributeType, Math.round(Correlation.median(valuesFiltered)))));

			if (canceller.isCancelled()) {
				return result;
			}

			long max = NumberUtils.max(valuesFiltered);
			result.add(c(attribute, Field.max, DisplayType.create(attributeType, max)));

			if (canceller.isCancelled()) {
				return result;
			}

			double minFitness = Array.min(fitnessFiltered);
			result.add(c(attribute, Field.minFitness, DisplayType.numeric(minFitness)));

			if (canceller.isCancelled()) {
				return result;
			}

			BigDecimal meanFitness = Correlation.mean(fitnessFiltered);
			result.add(c(attribute, Field.averageFitness, DisplayType.numeric(meanFitness.doubleValue())));

			if (canceller.isCancelled()) {
				return result;
			}

			double maxFitness = Array.max(fitnessFiltered);
			result.add(c(attribute, Field.maxFitness, DisplayType.numeric(maxFitness)));

			if (canceller.isCancelled()) {
				return result;
			}

			if (min == max) {
				result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
				result.add(c(attribute, Field.correlation, DisplayType.NA()));
				result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.add(c(attribute, Field.standardDeviation,
						DisplayType.create(Type.duration, Math.round(standardDeviation))));

				if (canceller.isCancelled()) {
					return result;
				}

				if (minFitness == maxFitness) {
					result.add(c(attribute, Field.correlation, DisplayType.NA()));
					result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
				} else {
					double correlation = Correlation
							.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation)
							.doubleValue();
					if (correlation == -Double.MAX_VALUE) {
						result.add(c(attribute, Field.correlation, DisplayType.NA()));
						result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
					} else {
						result.add(c(attribute, Field.correlation, DisplayType.numeric(correlation)));

						if (canceller.isCancelled()) {
							return result;
						}

						BufferedImage plot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered, min,
								max, "fitness", fitnessFiltered, minFitness, maxFitness);
						result.add(c(attribute, Field.correlationPlot, DisplayType.image(plot)));
					}
				}
			}
		}

		return result;
	}

	private static List<DataRow<Object>> createAttributeDataLiteral(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, double[] trace2fitness, IvMCanceller canceller) {
		assert !attribute.isVirtual();

		List<DataRow<Object>> result = new ArrayList<>();

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
			return result;
		}

		result.add(c(attribute, Field.tracesWithAttribute, DisplayType.numeric(numberOfTracesWithAttribute)));

		ArrayList<String> valueSet = new ArrayList<>(attribute.getStringValues());
		result.add(c(attribute, Field.numberOfDifferentValues, DisplayType.numeric(valueSet.size())));

		if (valueSet.isEmpty()) {
			result.add(c(attribute, Field.first, DisplayType.NA()));
			result.add(c(attribute, Field.last, DisplayType.NA()));
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
			result.add(c(attribute, Field.first, DisplayType.literal(valueSet.get(first))));
			result.add(c(attribute, Field.last, DisplayType.literal(valueSet.get(last))));
		}

		return result;
	}

	private static List<DataRow<Object>> createAttributeDataDuration(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, double[] trace2fitness, IvMCanceller canceller) {
		List<DataRow<Object>> result = new ArrayList<>();

		Type attributeType = Type.duration;

		//compute correlation and plots
		double[] fitnessFiltered;
		long[] valuesFiltered;
		{
			long[] values = new long[numberOfTraces];
			int i = 0;
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				long value = AttributeUtils.valueLong(attribute, trace);

				//store the value
				values[i] = value;

				i++;
			}

			if (canceller.isCancelled()) {
				return result;
			}

			//filter missing values
			Pair<long[], double[]> p = Correlation.filterMissingValues(values, trace2fitness);
			valuesFiltered = p.getA();
			fitnessFiltered = p.getB();
		}

		//we assume we always have a fitness value, so we can use the filtered lists

		if (canceller.isCancelled()) {
			return result;
		}

		result.add(c(attribute, Field.tracesWithAttribute, DisplayType.numeric(valuesFiltered.length)));

		//if the list is empty, better fail now and do not attempt the rest
		if (valuesFiltered.length == 0) {
			result.add(c(attribute, Field.min, DisplayType.NA()));
			result.add(c(attribute, Field.average, DisplayType.NA()));
			result.add(c(attribute, Field.median, DisplayType.NA()));
			result.add(c(attribute, Field.max, DisplayType.NA()));
			result.add(c(attribute, Field.minFitness, DisplayType.NA()));
			result.add(c(attribute, Field.averageFitness, DisplayType.NA()));
			result.add(c(attribute, Field.maxFitness, DisplayType.NA()));
			result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
			result.add(c(attribute, Field.correlation, DisplayType.NA()));
			result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
		} else {
			long min = NumberUtils.min(valuesFiltered);
			result.add(c(attribute, Field.min, DisplayType.create(attributeType, min)));

			if (canceller.isCancelled()) {
				return result;
			}

			BigDecimal valuesAverage = Correlation.mean(valuesFiltered);
			result.add(c(attribute, Field.average, DisplayType.create(attributeType, valuesAverage.longValue())));

			if (canceller.isCancelled()) {
				return result;
			}

			result.add(c(attribute, Field.median,
					DisplayType.create(attributeType, Math.round(Correlation.median(valuesFiltered)))));

			if (canceller.isCancelled()) {
				return result;
			}

			long max = NumberUtils.max(valuesFiltered);
			result.add(c(attribute, Field.max, DisplayType.create(attributeType, max)));

			if (canceller.isCancelled()) {
				return result;
			}

			double minFitness = Array.min(fitnessFiltered);
			result.add(c(attribute, Field.minFitness, DisplayType.numeric(minFitness)));

			if (canceller.isCancelled()) {
				return result;
			}

			BigDecimal meanFitness = Correlation.mean(fitnessFiltered);
			assert meanFitness != null;
			result.add(c(attribute, Field.averageFitness, DisplayType.numeric(meanFitness.doubleValue())));

			if (canceller.isCancelled()) {
				return result;
			}

			double maxFitness = Array.max(fitnessFiltered);
			result.add(c(attribute, Field.maxFitness, DisplayType.numeric(maxFitness)));

			if (canceller.isCancelled()) {
				return result;
			}

			if (min == max) {
				result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
				result.add(c(attribute, Field.correlation, DisplayType.NA()));
				result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.add(c(attribute, Field.standardDeviation,
						DisplayType.create(attributeType, Math.round(standardDeviation))));

				if (canceller.isCancelled()) {
					return result;
				}

				if (minFitness == maxFitness) {
					result.add(c(attribute, Field.correlation, DisplayType.NA()));
					result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
				} else {
					double correlation = Correlation
							.correlation(fitnessFiltered, valuesFiltered, valuesAverage, standardDeviation)
							.doubleValue();
					if (correlation == -Double.MAX_VALUE) {
						result.add(c(attribute, Field.correlation, DisplayType.NA()));
						result.add(c(attribute, Field.correlationPlot, DisplayType.NA()));
					} else {
						result.add(c(attribute, Field.correlation, DisplayType.numeric(correlation)));

						if (canceller.isCancelled()) {
							return result;
						}

						BufferedImage plot = CorrelationDensityPlot.create(attribute.getName(), valuesFiltered, min,
								max, "fitness", fitnessFiltered, minFitness, maxFitness);
						result.add(c(attribute, Field.correlationPlot, DisplayType.image(plot)));
					}
				}
			}
		}

		return result;
	}

	private static DataRow<Object> c(Attribute attribute, Field field, DisplayType value) {
		return new DataRow<Object>(value, attribute.getName(), field.toString());
	}

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
				return "average fitness of traces with attribute";
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

	private static double getDoubleMin(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMin();
		} else if (attribute.isDuration()) {
			return attribute.getDurationMin();
		} else {
			return attribute.getTimeMin();
		}
	}

	private static double getDoubleMax(Attribute attribute) {
		if (attribute.isNumeric()) {
			return attribute.getNumericMax();
		} else if (attribute.isDuration()) {
			return attribute.getDurationMax();
		} else {
			return attribute.getTimeMax();
		}
	}
}
