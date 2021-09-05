package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelAbstract;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelComputer;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactory;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelsImpl;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

import lpsolve.LpSolveException;

public class Cl19DataAnalysisCost<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl19 cost";
	}

	public String getStatusBusyMessage() {
		return "Fitting cost model..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.log_timestamps_logical, IvMObject.selected_cost_model_factory,
				IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.aligned_log_info_filtered };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_cost_models };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		boolean timestampsLogical = inputs.get(IvMObject.log_timestamps_logical);

		if (!timestampsLogical) {
			return null;
		}

		IvMModel model = inputs.get(IvMObject.model);
		IvMLogFiltered log = inputs.get(IvMObject.aligned_log_filtered);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);
		CostModelFactory costModelFactory = inputs.get(IvMObject.selected_cost_model_factory);

		Pair<CostModel, String> p = computeCostModel(costModelFactory, model, logInfo, log, canceller);
		CostModel costModel = p.getA();
		String costModelMessage = p.getB();

		CostModel costModelNegative = null;
		String negativeCostModelMessage = null;
		if (log.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = log.clone();
			negativeLog.invert();
			IvMLogInfo negativeLogInfo = new IvMLogInfo(negativeLog, model);

			Pair<CostModel, String> p2 = computeCostModel(costModelFactory, model, negativeLogInfo, negativeLog,
					canceller);

			costModelNegative = p2.getA();
			negativeCostModelMessage = p2.getB();
		}

		CostModelsImpl result = new CostModelsImpl(costModel, costModelNegative, log.isSomethingFiltered());
		result.setCostModelMessage(costModelMessage);
		result.setNegativeCostModelMessage(negativeCostModelMessage);

		return new IvMObjectValues().//
				s(IvMObject.data_analysis_cost_models, result);
	}

	private static Pair<CostModel, String> computeCostModel(CostModelFactory costModelFactory, IvMModel model,
			IvMLogInfo logInfo, IvMLogFiltered log, IvMCanceller canceller) throws LpSolveException {
		CostModelComputer costModelComputer = costModelFactory.createComputer();
		CostModelAbstract costModel = costModelFactory.createCostModel(model, logInfo);
		costModelComputer.compute(model, log, logInfo, costModel, canceller);

		//set user info
		costModel.getModelProperties()
				.add(new DataRow<Object>(DisplayType.literal(costModelComputer.getName()), "cost model", "fitter"));
		costModel.getModelProperties()
				.add(new DataRow<Object>(DisplayType.literal(costModel.getName()), "cost model", "type"));

		String costModelMessage = costModelComputer.getErrorMessage();
		if (costModelMessage != null) {
			return Pair.of(null, costModelMessage);
		} else {
			return Pair.of((CostModel) costModel, null);
		}
	}

}
