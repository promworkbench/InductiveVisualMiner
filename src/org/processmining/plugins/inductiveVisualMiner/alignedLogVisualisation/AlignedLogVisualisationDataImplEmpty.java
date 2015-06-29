package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplEmpty implements AlignedLogVisualisationData {

	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(1l, 1l);
	}

	public Triple<String, Long, String> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return Triple.of("", 1l, "");
	}

	public Triple<String, Long, String> getModelMoveEdgeLabel(UnfoldedNode unode) {
		return Triple.of("", 1l, "");
	}

	public Triple<String, MultiSet<XEventClass>, String> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return Triple.of("", new MultiSet<XEventClass>(), "");
	}

	public Triple<String, Long, String> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return Triple.of("", 1l, "");
	}

	public void setTime(long time) {
		
	}
}
