package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceComputer;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCombination;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueMineActivityLog;

import gnu.trove.map.TIntObjectMap;

public class Cl14PerformanceNegative<C> extends DataChainLinkComputationAbstract<C> {

	@Override
	public String getName() {
		return "measure performance-negative";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Measuring performance -..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.log_timestamps_logical, IvMObject.model, IvMObject.aligned_log_filtered,
				IvMObject.performance, IvMObject.data_analyses_delay };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.performance_negative };
	}

	@Override
	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		boolean timestampsLogical = inputs.get(IvMObject.log_timestamps_logical);

		if (!timestampsLogical) {
			return null;
		}
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogFiltered log = inputs.get(IvMObject.aligned_log_filtered);

		if (!log.isSomethingFiltered()) {
			return new IvMObjectValues().//
					s(IvMObject.performance_negative, inputs.get(IvMObject.performance));
		}

		log = log.clone();
		log.invert();

		TIntObjectMap<QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(model, log);

		QueueLengths queueLengths = new QueueLengthsImplCombination(queueActivityLogs);

		Performance performance = PerformanceComputer.compute(log, model, queueLengths, queueActivityLogs, canceller);

		return new IvMObjectValues().//
				s(IvMObject.performance_negative, performance);
	}
}