package org.processmining.plugins.inductiveVisualMiner.histogram;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

/**
 * There are two types of histograms: a global one denoting the number of active
 * cases in the system, and a node-specific one denoting the executions over
 * time of a particular node.
 * 
 * These histograms are reality-based, i.e. no timestamps are invented and no
 * fading-in/out time is added to traces. Therefore, histograms might deviate
 * from the animation.
 * 
 * @author sleemans
 *
 */

public class HistogramData {
	private final Scaler scaler;

	private final int[] globalCountFiltered;
	private final int[] globalCountUnfiltered;
	private final int globalBuckets;
	private double globalMax;

	private final TIntObjectMap<int[]> localCountFiltered;
	private final TIntObjectMap<int[]> localCountUnfiltered;
	private final int localBuckets;
	private double localMax;

	/**
	 * 
	 * @param tree
	 * @param log
	 * @param scaler
	 * @param globalBuckets
	 * @param localBuckets
	 *            The width of the histogram (used for pixel-precision).
	 * @param canceller
	 */
	public HistogramData(IvMEfficientTree tree, IvMLogFiltered log, Scaler scaler, int globalBuckets, int localBuckets,
			IvMCanceller canceller) {
		this.scaler = scaler;

		globalCountFiltered = new int[globalBuckets];
		globalCountUnfiltered = new int[globalBuckets];
		this.globalBuckets = globalBuckets;
		globalMax = 0;

		//initialise local
		this.localBuckets = localBuckets;
		this.localCountFiltered = new TIntObjectHashMap<int[]>(10, 0.5f, -1);
		this.localCountUnfiltered = new TIntObjectHashMap<int[]>(10, 0.5f, -1);
		for (int node : tree.getAllNodes()) {
			localCountFiltered.put(node, new int[localBuckets]);
			localCountUnfiltered.put(node, new int[localBuckets]);
		}
		localMax = 0;

		for (IteratorWithPosition<IvMTrace> it = log.iteratorUnfiltered(); it.hasNext();) {
			if (canceller.isCancelled()) {
				return;
			}
			IvMTrace trace = it.next();
			boolean isFilteredOut = log.isFilteredOut(it.getPosition());

			addTraceGlobal(trace, isFilteredOut);
			addTraceLocal(tree, trace, isFilteredOut);
		}
	}

	private void addTraceGlobal(IvMTrace trace, boolean isFilteredOut) {

		Long realStartTime = trace.getRealStartTime();
		Long realEndTime = trace.getRealEndTime();

		if (realStartTime != null) {
			int startBucket = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(realStartTime)) * (globalBuckets - 1));
			int endBucket = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(realEndTime)) * (globalBuckets - 1));

			for (int i = startBucket; i <= endBucket; i++) {
				globalCountUnfiltered[i]++;
				globalMax = Math.max(globalMax, globalCountUnfiltered[i]);

				if (!isFilteredOut) {
					globalCountFiltered[i]++;
				}
			}
		}
	}

	/**
	 * Add a trace to the node-specific histograms.
	 * 
	 * @param trace
	 * @param isFilteredOut
	 */
	private void addTraceLocal(IvMEfficientTree tree, IvMTrace trace, boolean isFilteredOut) {
		//walk over the activity instances of the trace
		for (ActivityInstanceIterator it = trace.activityInstanceIterator(tree); it.hasNext();) {
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> t = it.next();
			if (t != null) {
				Integer unode = t.getA();
				IvMMove moveStart = t.getE();
				IvMMove moveComplete = t.getF();

				int startBucket = -1;
				int endBucket = -1;
				if (moveComplete != null && moveComplete.getLogTimestamp() != null) {
					endBucket = (int) (scaler
							.userTime2Fraction(scaler.logTime2UserTime(moveComplete.getLogTimestamp())) * (localBuckets - 1));
					if (moveStart != null && moveStart.getLogTimestamp() != null) {
						startBucket = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(moveStart
								.getLogTimestamp())) * (localBuckets - 1));
					} else {
						//if the start time stamp is missing, add the activity to the end bucket
						startBucket = endBucket;
					}
				} else if (moveStart != null && moveStart.getLogTimestamp() != null) {
					//there's only a start time stamp. Use that for a single bucket;
					startBucket = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(moveStart.getLogTimestamp())) * localBuckets);
					endBucket = startBucket;
				}

				if (endBucket != -1) {
					for (int i = startBucket; i <= endBucket; i++) {
						localCountUnfiltered.get(unode)[i]++;
						localMax = Math.max(localMax, localCountUnfiltered.get(unode)[i]);

						if (!isFilteredOut) {
							localCountFiltered.get(unode)[i]++;
						}
					}
				}
			}
		}
	}

	public double getLogTimeInMsPerLocalBucket() {
		return (scaler.getMaxInLogTime() - scaler.getMinInLogTime()) / localBuckets;
	}

	public int getNrOfGlobalBuckets() {
		return globalBuckets;
	}

	public int getNrOfLocalBuckets() {
		return localBuckets;
	}

	public double getGlobalBucketFraction(int bucketNr) {
		return globalCountFiltered[bucketNr] / globalMax;
	}

	public double getLocalBucketFraction(int node, int pixel) {
		return localCountFiltered.get(node)[pixel] / localMax;
	}

	public int getGlobalMaximum() {
		return (int) globalMax;
	}

	public int getLocalMaximum() {
		return (int) localMax;
	}
}
