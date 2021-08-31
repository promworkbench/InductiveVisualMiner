package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.cost.ComputeCostModel;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class Cl19Cost<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl19 cost";
	}

	public String getStatusBusyMessage() {
		return "Fitting cost model";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.log_timestamps_logical, IvMObject.model,
				IvMObject.aligned_log_filtered, };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.cost_model };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		boolean timestampsLogical = inputs.get(IvMObject.log_timestamps_logical);

		if (!timestampsLogical) {
			return null;
		}

		IvMModel model = inputs.get(IvMObject.model);
		IvMLogFiltered log = inputs.get(IvMObject.aligned_log_filtered);

		CostModel costModel = ComputeCostModel.compute(model, log, canceller);
		
		if (costModel == null) {
			return null;
		}

		return new IvMObjectValues().//
				s(IvMObject.cost_model, costModel);
	}

}
