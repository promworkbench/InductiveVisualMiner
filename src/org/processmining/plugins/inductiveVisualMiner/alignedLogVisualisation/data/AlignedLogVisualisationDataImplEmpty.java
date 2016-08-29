package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;

public class AlignedLogVisualisationDataImplEmpty implements AlignedLogVisualisationData {

	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(0l, 0l);
	}

	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves) {
		return Triple.of("", 0l, 0l);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(int unode) {
		return Pair.of("", 0l);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return Pair.of("", new MultiSet<XEventClass>());
	}

	public Pair<String, Long> getEdgeLabel(int unode, boolean includeModelMoves) {
		return Pair.of("", 0l);
	}

	public void setTime(long time) {
		
	}
}
