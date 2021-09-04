package org.processmining.plugins.inductiveVisualMiner.cost;

/**
 * The result object of the cost models computation. Keeps track of two cost
 * models.
 * 
 * @author sander
 *
 */
public interface CostModels {

	public boolean isBothModelsWereAttempted();

	public CostModel getCostModel();

	public String getCostModelMessage();

	public CostModel getNegativeCostModel();

	public String getNegativeCostModelMessage();
}
