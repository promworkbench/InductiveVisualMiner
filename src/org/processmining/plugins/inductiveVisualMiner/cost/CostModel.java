package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public interface CostModel {

	/**
	 * The representation to show this model on a node. Value (for colouring)
	 * and string value.
	 * 
	 * @param node
	 * @return
	 */
	public Pair<Long, String> getNodeRepresentationModel(int node);

	public String[][] getNodeRepresentationPopup(int unode);

	/**
	 * 
	 * @param trace
	 * @return the cost associated with the trace, or Double.NaN if it does not
	 *         exist.
	 */
	public double getCost(IvMTrace trace);

}