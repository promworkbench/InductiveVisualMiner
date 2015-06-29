package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplSojourn implements AlignedLogVisualisationData {

	private long minQueueLength;
	private long maxQueueLength;

	private final QueueLengthsWrapper queueLengths;

	private final AlignedLogVisualisationData dataForEdges;

	public AlignedLogVisualisationDataImplSojourn(ProcessTree tree, QueueLengthsWrapper queueLengths,
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

	public Triple<String, Long, String> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long length = Math.round(queueLengths.getSojournTime(unode));
		if (length >= 0) {
			return Triple.of("sojourn ", length, " ms");
		} else {
			return Triple.of("-", -1l, "");
		}
	}

	public Triple<String, Long, String> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return dataForEdges.getEdgeLabel(unode, includeModelMoves);
	}

	public Triple<String, Long, String> getModelMoveEdgeLabel(UnfoldedNode unode) {
		return dataForEdges.getModelMoveEdgeLabel(unode);
	}

	public Triple<String, MultiSet<XEventClass>, String> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return dataForEdges.getLogMoveEdgeLabel(logMovePosition);
	}

}
