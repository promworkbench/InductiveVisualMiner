package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;

public interface AlignedLogVisualisationData {

	/**
	 * 
	 * @return result[0] = minimum, result[1] = maximum
	 * @throws UnknownTreeNodeException
	 */
	public Pair<Long, Long> getExtremeCardinalities() throws UnknownTreeNodeException;

	/**
	 * 
	 * @param node
	 * @param includeModelMoves
	 * @return Triple of label (second line), first colour, second colour (for
	 *         gradient). Leave colours null if unwanted.
	 * @throws UnknownTreeNodeException
	 */
	public Triple<String, Long, Long> getNodeLabel(int node, boolean includeModelMoves) throws UnknownTreeNodeException;

	public Pair<String, Long> getEdgeLabel(int node, boolean includeModelMoves) throws UnknownTreeNodeException;

	public Pair<String, Long> getModelMoveEdgeLabel(int node);

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition);

	public void setTime(long time);
}
