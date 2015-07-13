package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplSojourn implements AlignedLogVisualisationData {

	protected long minQueueLength;
	protected long maxQueueLength;

	protected final PerformanceWrapper queueLengths;

	protected final AlignedLogVisualisationData dataForEdges;

	public AlignedLogVisualisationDataImplSojourn(ProcessTree tree, PerformanceWrapper queueLengths,
			AlignedLogInfo logInfo) {
		this.queueLengths = queueLengths;
		dataForEdges = new AlignedLogVisualisationDataImplFrequencies(tree, logInfo);

		//compute extrem sojourn times
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

	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long length = Math.round(queueLengths.getSojournTime(unode));
		if (length >= 0) {
			return Pair.of(Performance.timeToString(length), length);
		} else {
			return Pair.of("-", -1l);
		}
	}

	public Pair<String, Long> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return dataForEdges.getEdgeLabel(unode, includeModelMoves);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(UnfoldedNode unode) {
		return dataForEdges.getModelMoveEdgeLabel(unode);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return dataForEdges.getLogMoveEdgeLabel(logMovePosition);
	}

}
