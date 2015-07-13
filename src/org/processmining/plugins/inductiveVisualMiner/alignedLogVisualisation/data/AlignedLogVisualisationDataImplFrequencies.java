package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplFrequencies implements AlignedLogVisualisationData {

	private final ProcessTree tree;
	private final AlignedLogInfo logInfo;

	public AlignedLogVisualisationDataImplFrequencies(ProcessTree tree, AlignedLogInfo logInfo) {
		this.tree = tree;
		this.logInfo = logInfo;
	}

	public Pair<Long, Long> getExtremeCardinalities() {
		return AlignedLogMetrics.getExtremes(new UnfoldedNode(tree.getRoot()), logInfo);
	}

	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}
	
	public Pair<String, Long> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(UnfoldedNode unode) {
		long cardinality = AlignedLogMetrics.getModelMovesLocal(unode, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		MultiSet<XEventClass> cardinality = AlignedLogMetrics.getLogMoves(logMovePosition, logInfo);
		return Pair.of(String.valueOf(cardinality.size()), cardinality);
	}

	public void setTime(long time) {
		
	}
}
