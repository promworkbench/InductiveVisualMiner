package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.THashMap;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplBPTComplete extends QueueLengths {

	private final static double theta = 1.5;

	private final QueueLengths cli;
	private final Map<UnfoldedNode, TLongArrayList> busyPeriods;

	public QueueLengthsImplBPTComplete(Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		cli = new QueueLengthsImplCLIComplete(queueActivityLogs, 2);
		busyPeriods = new THashMap<UnfoldedNode, TLongArrayList>();
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			busyPeriods.put(unode, findBusyPeriods(unode, queueActivityLogs.get(unode)));
		}
	}

	private TLongArrayList findBusyPeriods(UnfoldedNode unode, QueueActivityLog l) {
		//make a list of timestamps
		long[] timestamps = new long[l.size() * 2];
		for (int i = 0; i < l.size(); i++) {
			timestamps[i * 2] = l.getInitiate(i);
			timestamps[i * 2 + 1] = l.getComplete(i);
		}
		Arrays.sort(timestamps);
		
		System.out.println(timestamps.length);

		//for each timestamp, determine whether it is a busy period
		BitSet busyPeriods = new BitSet();
		for (int i = 0; i < timestamps.length; i++) {
			if (timestamps[i] > 0) {
				busyPeriods.set(i, isBusy(unode, l, timestamps[i]));
			} else {
				busyPeriods.set(i, false);
			}
		}
		
		System.out.println(busyPeriods);

		//make a list of timestamps where busy periods change
		boolean isBusy = false;
		TLongArrayList result = new TLongArrayList();
		for (int i = 0; i < timestamps.length; i++) {
			if (busyPeriods.get(i) != isBusy) {
				System.out.println("change at timestamp #" +i + " @" + timestamps[i]);
				isBusy = !isBusy;
				result.add(timestamps[i]);
			}
		}

		System.out.println(result);
		return result;
	}

	public boolean isBusy(UnfoldedNode unode, QueueActivityLog l, long time) {
		double queueLength = 0;
		for (int index = 0; index < l.size(); index++) {
			queueLength += cli.getQueueProbability(unode, l, time, index);
		}
		return queueLength > theta;
	}

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getComplete(traceIndex) > 0) {

			//find the interval we're in and whether it's busy
			TLongArrayList intervals = busyPeriods.get(unode);
			boolean busyPeriod = false;
			long beginOfBusyPeriod = 0;
			for (int i = 0; i < intervals.size(); i++) {
				if (intervals.get(i) > time) {
					break;
				}

				beginOfBusyPeriod = intervals.get(i);
				busyPeriod = !busyPeriod;
			}

			//not busy period: no queue
			if (!busyPeriod) {
				return 0;
			}

			//busy period
			double result = 0;
			if (beginOfBusyPeriod <= l.getInitiate(traceIndex) && l.getInitiate(traceIndex) <= time) {
				result += 1;
			}
			if (beginOfBusyPeriod <= l.getComplete(traceIndex) && l.getComplete(traceIndex) <= time) {
				result -= 1;
			}

			return result;
		}
		return 0;
	}
}
