package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplFrequencies implements AlignedLogVisualisationData {

	private final ProcessTree tree;
	private final IvMLogInfo logInfo;

	public AlignedLogVisualisationDataImplFrequencies(ProcessTree tree, IvMLogInfo logInfo) {
		this.tree = tree;
		this.logInfo = logInfo;
	}

	public Pair<Long, Long> getExtremeCardinalities() {
		return IvMLogMetrics.getExtremes(new UnfoldedNode(tree.getRoot()), logInfo);
	}

	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}
	
	public Pair<String, Long> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(UnfoldedNode unode) {
		long cardinality = IvMLogMetrics.getModelMovesLocal(unode, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		MultiSet<XEventClass> cardinality = IvMLogMetrics.getLogMoves(logMovePosition, logInfo);
		return Pair.of(String.valueOf(cardinality.size()), cardinality);
	}

	public void setTime(long time) {
		
	}
}
