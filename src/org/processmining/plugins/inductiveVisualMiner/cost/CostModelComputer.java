package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

public interface CostModelComputer {

	public String getName();

	public void compute(IvMModel model, IvMLogFiltered log, IvMLogInfo logInfo, CostModelAbstract result,
			IvMCanceller canceller) throws Exception;

	/**
	 * 
	 * @return null if the computation was successful. Otherwise an error
	 *         message.
	 */
	public String getErrorMessage();

}