package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public interface AlignedLogVisualisationData {
	
	/**
	 * 
	 * @return result[0] = minimum, result[1] = maximum
	 */
	public Pair<Long, Long> getExtremeCardinalities();
	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves);
	public Pair<String, Long> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves);
	public Pair<String, Long> getModelMoveEdgeLabel(UnfoldedNode unode);
	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition);
	
	public void setTime(long time);
}
