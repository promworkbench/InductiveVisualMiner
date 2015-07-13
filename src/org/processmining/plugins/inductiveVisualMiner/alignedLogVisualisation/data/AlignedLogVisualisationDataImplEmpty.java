package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplEmpty implements AlignedLogVisualisationData {

	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(1l, 1l);
	}

	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return Pair.of("", 1l);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(UnfoldedNode unode) {
		return Pair.of("", -1l);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return Pair.of("", new MultiSet<XEventClass>());
	}

	public Pair<String, Long> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return Pair.of("", -1l);
	}

	public void setTime(long time) {
		
	}
}
