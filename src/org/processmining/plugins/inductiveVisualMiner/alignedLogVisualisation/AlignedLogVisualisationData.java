package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public interface AlignedLogVisualisationData {
	
	/**
	 * 
	 * @return result[0] = minimum, result[1] = maximum
	 */
	public Pair<Long, Long> getExtremeCardinalities();
	public Triple<String, Long, String> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves);
	public Triple<String, Long, String> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves);
	public Triple<String, Long, String> getModelMoveEdgeLabel(UnfoldedNode unode);
	public Triple<String, MultiSet<XEventClass>, String> getLogMoveEdgeLabel(LogMovePosition logMovePosition);
	
	public void setTime(long time);
}
