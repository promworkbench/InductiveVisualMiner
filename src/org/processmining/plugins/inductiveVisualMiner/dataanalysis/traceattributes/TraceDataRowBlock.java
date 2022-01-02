package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.math.plot.utils.Array;
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

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();
			int numberOfTracesNegative = getNumberOfTraces(negativeLog);

			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(merge(createAttributeData(logFiltered, attribute, numberOfTraces, canceller),
						createAttributeData(negativeLog, attribute, numberOfTracesNegative, canceller), canceller));
			}
		} else {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(createAttributeData(logFiltered, attribute, numberOfTraces, canceller));
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
			int numberOfTraces, IvMCanceller canceller) {
		if (attribute.isNumeric()) {
			return createAttributeDataNumeric(logFiltered, attribute, numberOfTraces, canceller);
		} else if (attribute.isBoolean()) {
			return createAttributeDataBoolean(logFiltered, attribute, canceller);
		} else if (attribute.isTime()) {
			return createAttributeDataTime(logFiltered, attribute, numberOfTraces, canceller);
		} else if (attribute.isLiteral()) {
			return createAttributeDataLiteral(logFiltered, attribute, numberOfTraces, canceller);
		} else if (attribute.isDuration()) {
			return createAttributeDataDuration(logFiltered, attribute, numberOfTraces, canceller);
		}

		List<DataRow<Object>> result = new ArrayList<>();
		result.add(new DataRow<Object>(DisplayType.literal("[not supported]"), attribute.getName(), ""));
		return result;
	}

	private static List<DataRow<Object>> createAttributeDataNumeric(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, IvMCanceller canceller) {
		Type attributeType = DisplayType.fromAttribute(attribute);

		List<DataRow<Object>> result = new ArrayList<>();

		//compute correlation and plots
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
			valuesFiltered = Correlation.filterMissingValues(values);
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
			result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
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

			if (min == max) {
				result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.add(c(attribute, Field.standardDeviation, DisplayType.create(attributeType, standardDeviation)));

				if (canceller.isCancelled()) {
					return result;
				}
			}
		}

		return result;
	}

	private static List<DataRow<Object>> createAttributeDataBoolean(IvMLogFiltered logFiltered, Attribute attribute,
			IvMCanceller canceller) {
		List<DataRow<Object>> result = new ArrayList<>();

		//compute correlation and plots
		int countTrue = 0;
		int countFalse = 0;
		{
			for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				Boolean value = AttributeUtils.valueBoolean(attribute, trace);

				if (value != null) {
					if (value) {
						countTrue++;
					} else {
						countFalse++;
					}
				}
			}

			if (canceller.isCancelled()) {
				return result;
			}
		}

		result.add(c(attribute, Field.tracesWithAttribute, DisplayType.numeric(countTrue + countFalse)));
		result.add(c(attribute, Field.tracesWithValueTrue, DisplayType.numeric(countTrue)));
		result.add(c(attribute, Field.tracesWithValueFalse, DisplayType.numeric(countFalse)));

		return result;
	}

	private static List<DataRow<Object>> createAttributeDataTime(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, IvMCanceller canceller) {
		Type attributeType = Type.time;

		List<DataRow<Object>> result = new ArrayList<>();

		//compute correlation and plots
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
			valuesFiltered = Correlation.filterMissingValues(values);
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
			result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
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

			if (min == max) {
				result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.add(c(attribute, Field.standardDeviation,
						DisplayType.create(Type.duration, Math.round(standardDeviation))));

				if (canceller.isCancelled()) {
					return result;
				}
			}
		}

		return result;
	}

	private static List<DataRow<Object>> createAttributeDataLiteral(IvMLogFiltered logFiltered, Attribute attribute,
			int numberOfTraces, IvMCanceller canceller) {
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
			int numberOfTraces, IvMCanceller canceller) {
		List<DataRow<Object>> result = new ArrayList<>();

		Type attributeType = Type.duration;

		//compute correlation and plots
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
			valuesFiltered = Correlation.filterMissingValues(values);
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
			result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
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

			if (min == max) {
				result.add(c(attribute, Field.standardDeviation, DisplayType.NA()));
			} else {
				double standardDeviation = Correlation.standardDeviation(valuesFiltered, valuesAverage);
				result.add(c(attribute, Field.standardDeviation,
						DisplayType.create(attributeType, Math.round(standardDeviation))));
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
		tracesWithValueTrue {
			public String toString() {
				return "traces with true attribute value";
			}
		},
		tracesWithValueFalse {
			public String toString() {
				return "traces with false attribute value";
			}
		}
	}
}