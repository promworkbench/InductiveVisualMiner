package org.processmining.plugins.inductiveVisualMiner.cost;

public class CostModelsImpl implements CostModels {

	private final CostModel costModel;
	private final CostModel negativeCostModel;
	private final boolean bothModelsWereAttempted;
	private String costModelMessage;
	private String negativeCostModelMessage;

	public CostModelsImpl(CostModel costModel, CostModel negativeCostModel, boolean bothModelsWereAttempted) {
		this.costModel = costModel;
		this.negativeCostModel = negativeCostModel;
		this.bothModelsWereAttempted = bothModelsWereAttempted;
	}

	@Override
	public CostModel getCostModel() {
		return costModel;
	}

	@Override
	public CostModel getNegativeCostModel() {
		return negativeCostModel;
	}

	@Override
	public boolean isBothModelsWereAttempted() {
		return bothModelsWereAttempted;
	}

	@Override
	public String getCostModelMessage() {
		return costModelMessage;
	}

	public void setCostModelMessage(String costModelMessage) {
		this.costModelMessage = costModelMessage;
	}

	@Override
	public String getNegativeCostModelMessage() {
		return negativeCostModelMessage;
	}

	public void setNegativeCostModelMessage(String negativeCostModelMessage) {
		this.negativeCostModelMessage = negativeCostModelMessage;
	}

}
