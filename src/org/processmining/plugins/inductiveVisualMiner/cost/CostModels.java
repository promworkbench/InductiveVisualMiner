package org.processmining.plugins.inductiveVisualMiner.cost;

public interface CostModels {

	public boolean isBothModelsWereAttempted();

	public CostModel getCostModel();

	public String getCostModelMessage();

	public CostModel getNegativeCostModel();

	public String getNegativeCostModelMessage();
}
