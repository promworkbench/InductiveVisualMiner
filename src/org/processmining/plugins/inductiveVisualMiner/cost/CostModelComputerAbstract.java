package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public abstract class CostModelComputerAbstract implements CostModelComputer {

	protected String message = null;

	public String getErrorMessage() {
		return message;
	}

	public void compute(IvMModel model, IvMLogFiltered log, IvMLogInfo logInfoFiltered, CostModelAbstract result,
			IvMCanceller canceller) throws Exception {
		List<Pair<double[], Double>> data = getInputsAndCost(log, result, canceller);

		if (canceller.isCancelled()) {
			return;
		}

		if (data.size() == 0) {
			message = "no cost data available";
			return;
		}

		compute(data, result, canceller);
	}

	public abstract void compute(List<Pair<double[], Double>> data, CostModelAbstract result, IvMCanceller canceller)
			throws Exception;

	protected List<Pair<double[], Double>> getInputsAndCost(IvMLogFiltered log, CostModelAbstract result,
			IvMCanceller canceller) {
		//gather trace data
		List<Pair<double[], Double>> data = new ArrayList<>();
		{
			for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();

				if (canceller.isCancelled()) {
					return null;
				}

				Pair<double[], Double> p = result.getInputsAndCost(trace, canceller);

				if (p != null && StatUtils.sum(p.getA()) > 0) {
					data.add(p);
				}
			}
		}
		return data;
	}
}
