package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;

public class AlignedLogVisualisationDataImplSojourn implements AlignedLogVisualisationData {

	protected long minQueueLength;
	protected long maxQueueLength;

	protected PerformanceWrapper queueLengths;

	protected AlignedLogVisualisationData dataForEdges;

	public AlignedLogVisualisationDataImplSojourn(IvMModel model, PerformanceWrapper queueLengths, IvMLogInfo logInfo) {
		this.queueLengths = queueLengths;
		dataForEdges = new AlignedLogVisualisationDataImplFrequencies(model, logInfo);

		//compute extreme sojourn times
		minQueueLength = Long.MAX_VALUE;
		maxQueueLength = Long.MIN_VALUE;
		for (double d : queueLengths.getSojournTimes().values()) {
			if (d > maxQueueLength) {
				maxQueueLength = Math.round(d);
			}
			if (d < minQueueLength) {
				minQueueLength = Math.round(d);
			}
		}
	}

	public void setTime(long time) {

	}

	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(minQueueLength, maxQueueLength);
	}

	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves) {
		long length = Math.round(queueLengths.getSojournTime(unode));
		if (length >= 0) {
			return Triple.of(Performance.timeToString(length), length, length);
		} else {
			return Triple.of("-", -1l, -1l);
		}
	}

	public Pair<String, Long> getEdgeLabel(int unode, boolean includeModelMoves) throws UnknownTreeNodeException {
		return dataForEdges.getEdgeLabel(unode, includeModelMoves);
	}

	public Pair<String, Long> getEdgeLabel(int from, int to, boolean includeModelMoves)
			throws UnknownTreeNodeException {
		return dataForEdges.getEdgeLabel(from, to, includeModelMoves);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(int unode) {
		return dataForEdges.getModelMoveEdgeLabel(unode);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return dataForEdges.getLogMoveEdgeLabel(logMovePosition);
	}

	public AlignedLogVisualisationDataImplSojourn clone() throws CloneNotSupportedException {
		AlignedLogVisualisationDataImplSojourn c = (AlignedLogVisualisationDataImplSojourn) super.clone();

		c.dataForEdges = this.dataForEdges;
		c.maxQueueLength = this.maxQueueLength;
		c.minQueueLength = this.minQueueLength;
		c.queueLengths = this.queueLengths;

		return c;
	}

}
