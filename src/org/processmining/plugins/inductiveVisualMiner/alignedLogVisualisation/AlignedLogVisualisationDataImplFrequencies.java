package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
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

	public Triple<String, Long, String> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo);
		return Triple.of("", cardinality, "");
	}
	
	public Triple<String, Long, String> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo);
		return Triple.of("", cardinality, "");
	}

	public Triple<String, Long, String> getModelMoveEdgeLabel(UnfoldedNode unode) {
		long cardinality = AlignedLogMetrics.getModelMovesLocal(unode, logInfo);
		return Triple.of("", cardinality, "");
	}

	public Triple<String, MultiSet<XEventClass>, String> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		MultiSet<XEventClass> cardinality = AlignedLogMetrics.getLogMoves(logMovePosition, logInfo);
		return Triple.of("", cardinality, "");
	}

	public void setTime(long time) {
		
	}
}
