package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
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

	/**
	 * The representation of this model for this node (for the model-activity
	 * node popups).
	 * 
	 * @param unode
	 * @return
	 */
	public String[][] getNodeRepresentationPopup(int node);

	/**
	 * The representation of this model (for the data analysis cost tab)
	 * 
	 * @return
	 */
	public List<DataRow<Object>> getModelRepresentation();

	/**
	 * 
	 * @param trace
	 * @return the cost associated with the trace, or Double.NaN if it does not
	 *         exist.
	 */
	public double getCost(IvMTrace trace);

}