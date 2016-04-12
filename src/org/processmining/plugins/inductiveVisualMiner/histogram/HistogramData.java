package org.processmining.plugins.inductiveVisualMiner.histogram;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.chain.ChainLinkCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

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

	private final Map<UnfoldedNode, int[]> localCountFiltered;
	private final Map<UnfoldedNode, int[]> localCountUnfiltered;
	private final int localBuckets;
	private double localMax;

	/**
	 * 
	 * @param log
	 * @param scaler
	 * @param globalBuckets
	 *            the widht of the histogram (used for pixel-precision)
	 */
	public HistogramData(ProcessTree tree, IvMLogFiltered log, Scaler scaler, int globalBuckets, int localBuckets, ChainLinkCanceller canceller) {
		this.scaler = scaler;

		globalCountFiltered = new int[globalBuckets];
		globalCountUnfiltered = new int[globalBuckets];
		this.globalBuckets = globalBuckets;
		globalMax = 0;

		//initialise local
		this.localBuckets = localBuckets;
		this.localCountFiltered = new THashMap<>();
		this.localCountUnfiltered = new THashMap<>();
		for (UnfoldedNode unode : TreeUtils.unfoldAllActivities(tree)) {
			localCountFiltered.put(unode, new int[localBuckets]);
			localCountUnfiltered.put(unode, new int[localBuckets]);
		}
		localMax = 0;

		for (IteratorWithPosition<IvMTrace> it = log.iteratorUnfiltered(); it.hasNext();) {
			if (canceller.isCancelled()) {
				return;
			}
			IvMTrace trace = it.next();
			boolean isFilteredOut = log.isFilteredOut(it.getPosition());

			addTraceGlobal(trace, isFilteredOut);
			addTraceLocal(trace, isFilteredOut);
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
	private void addTraceLocal(IvMTrace trace, boolean isFilteredOut) {
		//walk over the activity instances of the trace
		for (ActivityInstanceIterator it = trace.activityInstanceIterator(); it.hasNext();) {
			Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> t = it.next();
			if (t != null) {
				UnfoldedNode unode = t.getA();
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

	public double getLocalBucketFraction(UnfoldedNode unode, int pixel) {
		return localCountFiltered.get(unode)[pixel] / localMax;
	}
	
	public int getGlobalMaximum() {
		return (int) globalMax;
	}
	
	public int getLocalMaximum() {
		return (int) localMax;
	}
}
