package org.processmining.plugins.inductiveVisualMiner.histogram;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

/**
 * There are three types of histograms: a global one denoting the number of
 * active cases in the system, a node-specific one denoting the executions over
 * time of a particular node, and an log-move edge specific one denoting the
 * happening of log moves at that place in the model.
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

	private final TIntObjectMap<int[]> localNodeCountFiltered;
	private final TIntObjectMap<int[]> localNodeCountUnfiltered;
	private final int localBuckets;
	private double localMax;

	private final TLongObjectMap<int[]> localEdgeCountFiltered;
	private final TLongObjectMap<int[]> localEdgeCountUnfiltered;

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
	public HistogramData(IvMEfficientTree tree, ProcessTreeVisualisationInfo info, IvMLogFiltered log, Scaler scaler,
			int globalBuckets, int localBuckets, IvMCanceller canceller) {
		this.scaler = scaler;

		globalCountFiltered = new int[globalBuckets];
		globalCountUnfiltered = new int[globalBuckets];
		this.globalBuckets = globalBuckets;
		globalMax = 0;

		this.localBuckets = localBuckets;
		localMax = 0;

		//initialise local nodes
		{
			this.localNodeCountFiltered = new TIntObjectHashMap<int[]>(10, 0.5f, -1);
			this.localNodeCountUnfiltered = new TIntObjectHashMap<int[]>(10, 0.5f, -1);
			for (int node : tree.getAllNodes()) {
				localNodeCountFiltered.put(node, new int[localBuckets]);
				localNodeCountUnfiltered.put(node, new int[localBuckets]);
			}
		}

		//initialise local edges
		{
			this.localEdgeCountFiltered = new TLongObjectHashMap<int[]>(10, 0.5f, -1);
			this.localEdgeCountUnfiltered = new TLongObjectHashMap<int[]>(10, 0.5f, -1);
			for (LocalDotEdge edge : info.getAllLogMoveEdges()) {
				long edgeIndex = getEdgeIndex(LogMovePosition.of(edge));
				localEdgeCountFiltered.put(edgeIndex, new int[localBuckets]);
				localEdgeCountUnfiltered.put(edgeIndex, new int[localBuckets]);
			}
		}

		for (IteratorWithPosition<IvMTrace> it = log.iteratorUnfiltered(); it.hasNext();) {
			if (canceller.isCancelled()) {
				return;
			}
			IvMTrace trace = it.next();
			boolean isFilteredOut = log.isFilteredOut(it.getPosition());

			addTraceGlobal(trace, isFilteredOut);
			addTraceLocalNode(tree, trace, isFilteredOut);
			if (!info.getAllLogMoveEdges().isEmpty()) {
				/*
				 * Process the trace for log moves. If there are no log move
				 * edges, it is not useful to do this.
				 */
				addTraceLocalEdge(trace, isFilteredOut);
			}
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
	private void addTraceLocalNode(IvMEfficientTree tree, IvMTrace trace, boolean isFilteredOut) {
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
						localNodeCountUnfiltered.get(unode)[i]++;
						localMax = Math.max(localMax, localNodeCountUnfiltered.get(unode)[i]);

						if (!isFilteredOut) {
							localNodeCountFiltered.get(unode)[i]++;
						}
					}
				}
			}
		}
	}

	/**
	 * Add log moves to their histograms.
	 * 
	 * @param trace
	 * @param isFilteredOut
	 */
	public void addTraceLocalEdge(IvMTrace trace, boolean isFilteredOut) {
		//walk over the trace
		for (IvMMove move : trace) {
			if (move.isLogMove() && move.getLogTimestamp() != null && move.isComplete()) {
				int bucket = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(move.getLogTimestamp())) * localBuckets);
				long edgeIndex = getEdgeIndex(LogMovePosition.of(move));
				localEdgeCountUnfiltered.get(edgeIndex)[bucket]++;
				localMax = Math.max(localMax, localEdgeCountUnfiltered.get(edgeIndex)[bucket]);

				if (!isFilteredOut) {
					localEdgeCountFiltered.get(edgeIndex)[bucket]++;
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

	public double getLocalNodeBucketFraction(int node, int pixel) {
		return localNodeCountFiltered.get(node)[pixel] / localMax;
	}

	public double getLocalEdgeBucketFraction(long edge, int pixel) {
		return localEdgeCountFiltered.get(edge)[pixel] / localMax;
	}

	public int getGlobalMaximum() {
		return (int) globalMax;
	}

	public int getLocalMaximum() {
		return (int) localMax;
	}

	public static long getEdgeIndex(LogMovePosition position) {
		return (((long) position.getOn()) << 32) | (position.getBeforeChild() & 0xffffffffL);
	}
}
