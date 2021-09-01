package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.cost.ComputeCostModel;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelsImpl;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

public class Cl19Cost<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl19 cost";
	}

	public String getStatusBusyMessage() {
		return "Fitting cost model";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.log_timestamps_logical, IvMObject.model, IvMObject.aligned_log_filtered,
				IvMObject.aligned_log_info_filtered };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.cost_models };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		boolean timestampsLogical = inputs.get(IvMObject.log_timestamps_logical);

		if (!timestampsLogical) {
			return null;
		}

		IvMModel model = inputs.get(IvMObject.model);
		IvMLogFiltered log = inputs.get(IvMObject.aligned_log_filtered);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);

		Pair<CostModel, String> p = ComputeCostModel.compute(model, log, logInfo, canceller);
		if (p == null) {
			return null;
		}
		CostModel costModel = p.getA();
		String costModelMessage = p.getB();

		CostModel costModelNegative = null;
		String negativeCostModelMessage = null;
		if (log.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = log.clone();
			negativeLog.invert();
			IvMLogInfo negativeLogInfo = new IvMLogInfo(negativeLog, model);
			Pair<CostModel, String> p2 = ComputeCostModel.compute(model, negativeLog, negativeLogInfo, canceller);
			if (p2 == null) {
				return null;
			}
			costModelNegative = p2.getA();
			negativeCostModelMessage = p2.getB();
		}

		CostModelsImpl result = new CostModelsImpl(costModel, costModelNegative, log.isSomethingFiltered());
		result.setCostModelMessage(costModelMessage);
		result.setNegativeCostModelMessage(negativeCostModelMessage);

		return new IvMObjectValues().//
				s(IvMObject.cost_models, result);
	}

}
