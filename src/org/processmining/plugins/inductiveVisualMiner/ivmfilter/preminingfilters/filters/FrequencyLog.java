package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

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
		final TObjectIntMap<int[]> frequencyTable;
		{
			frequencyTable = new TObjectIntCustomHashMap<>(new HashingStrategy<int[]>() {
				public int computeHashCode(int[] trace) {
					return Arrays.hashCode(trace);
				}

				public boolean equals(int[] o1, int[] o2) {
					return Arrays.equals(o1, o2);
				}
			});
			for (XTrace trace : xLog) {
				frequencyTable.adjustOrPutValue(trace2array(trace, classifier, eventClass2eventClassId), 1, 1);
			}
		}

		//create a mapping sorted index -> trace index
		int[] newIndex2traceIndex = new int[xLog.size()];
		{
			for (int i = 0; i < xLog.size(); i++) {
				newIndex2traceIndex[i] = i;
			}
			Collections.sort(Ints.asList(newIndex2traceIndex), new Comparator<Integer>() {
				public int compare(Integer o1, Integer o2) {
					int[] trace1 = trace2array(xLog.get(o1), classifier, eventClass2eventClassId);
					int[] trace2 = trace2array(xLog.get(o2), classifier, eventClass2eventClassId);
					return Integer.compare(frequencyTable.get(trace1), frequencyTable.get(trace2));
				}
			});
		}

		//invert the mapping
		traceIndex2newIndex = new int[xLog.size()];
		{
			for (int i = 0; i < xLog.size(); i++) {
				traceIndex2newIndex[newIndex2traceIndex[i]] = i;
			}
		}

		/**
		 * output: for each trace, the start of the bucket the trace is in.
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
