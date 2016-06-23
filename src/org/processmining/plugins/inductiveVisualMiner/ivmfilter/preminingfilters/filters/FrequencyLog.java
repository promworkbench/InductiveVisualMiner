package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import com.google.common.primitives.Ints;

public class FrequencyLog {
	private final int[] traceIndex2newIndex;
	private final int logSize;

	public FrequencyLog(final XLog xLog, final XEventClassifier classifier) {
		logSize = xLog.size();

		//initialise XEventClass->int map (do not rely on the buggy ProM implementation)
		final TObjectIntMap<String> eventClass2eventClassId = new TObjectIntCustomHashMap<>(
				new HashingStrategy<String>() {
					private static final long serialVersionUID = 1L;

					public int computeHashCode(String object) {
						return object.hashCode();
					}

					public boolean equals(String o1, String o2) {
						return o1.equals(o2);
					}
				}, 10, 0.5f, -1);

		//make a frequency table
		final TIntArrayList variantIndex2cardinality = new TIntArrayList();
		final TObjectIntMap<int[]> variant2variantIndex;
		{
			variant2variantIndex = new TObjectIntCustomHashMap<>(new HashingStrategy<int[]>() {

				private static final long serialVersionUID = 4894091764285361153L;

				public int computeHashCode(int[] trace) {
					return Arrays.hashCode(trace);
				}

				public boolean equals(int[] o1, int[] o2) {
					return Arrays.equals(o1, o2);
				}
			}, 10, 0.5f, -1);
			for (XTrace trace : xLog) {
				int key = variant2variantIndex.putIfAbsent(trace2array(trace, classifier, eventClass2eventClassId),
						variantIndex2cardinality.size());
				if (key == variant2variantIndex.getNoEntryValue()) {
					variantIndex2cardinality.add(1);
				} else {
					variantIndex2cardinality.set(key, variantIndex2cardinality.get(key) + 1);
				}
			}
		}

		//sort the variants on cardinality
		int[] variantOrder2variantIndex = new int[variant2variantIndex.size()];
		{
			for (int i = 0; i < variant2variantIndex.size(); i++) {
				variantOrder2variantIndex[i] = i;
			}
			Collections.sort(Ints.asList(variantOrder2variantIndex), new Comparator<Integer>() {
				public int compare(Integer o1, Integer o2) {
					return Integer.compare(variantIndex2cardinality.get(o2), variantIndex2cardinality.get(o1));
				}
			});
		}

		//invert the mapping
		int[] variantIndex2variantOrder = new int[variant2variantIndex.size()];
		{
			for (int i = 0; i < variant2variantIndex.size(); i++) {
				variantIndex2variantOrder[variantOrder2variantIndex[i]] = i;
			}
		}

		//make a mapping variantOrder -> start of bucket, which is sorted by cardinality
		int[] variantOrder2endBucket = new int[variant2variantIndex.size()];
		{
			//start with variantOrder -> cardinality
			for (int variantOrder = 0; variantOrder < variant2variantIndex.size(); variantOrder++) {
				variantOrder2endBucket[variantOrder] = variantIndex2cardinality
						.get(variantOrder2variantIndex[variantOrder]);
			}

			System.out.println(Arrays.toString(variantOrder2endBucket));

			//make it monotically increasing
			for (int variantOrder = 1; variantOrder < variant2variantIndex.size(); variantOrder++) {
				variantOrder2endBucket[variantOrder] = variantOrder2endBucket[variantOrder]
						+ variantOrder2endBucket[variantOrder - 1];
			}

			System.out.println(Arrays.toString(variantOrder2endBucket));
		}

		traceIndex2newIndex = new int[xLog.size()];
		{
			int i = 0;
			for (XTrace xTrace : xLog) {
				int[] trace = trace2array(xTrace, classifier, eventClass2eventClassId);
				int variantIndex = variant2variantIndex.get(trace);
				int variantOrder = variantIndex2variantOrder[variantIndex];
				traceIndex2newIndex[i] = variantOrder2endBucket[variantOrder];
				i++;
			}
		}
		
		System.out.println(Arrays.toString(traceIndex2newIndex));

		/**
		 * output: for each trace, the end of the bucket the trace is in.
		 */
	}

	public boolean isFrequentEnough(IMTrace trace, double threshold) {
		return traceIndex2newIndex[trace.getXTraceIndex()] <= threshold * logSize;
	}

	//transform a trace into an array of int
	public static int[] trace2array(XTrace trace, XEventClassifier classifier,
			TObjectIntMap<String> eventClass2eventClassId) {
		int[] result = new int[trace.size()];
		int i = 0;
		for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
			String activity = classifier.getClassIdentity(it.next());
			result[i] = eventClass2eventClassId.putIfAbsent(activity, eventClass2eventClassId.size());
			if (result[i] == eventClass2eventClassId.getNoEntryValue()) {
				result[i] = eventClass2eventClassId.size() - 1;
			}
			i++;
		}
		return result;
	}
}
