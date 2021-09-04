package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

/**
 * A factory provides two things: a computer and a model.
 * 
 * @author sander
 *
 */
public interface CostModelFactory {

	/**
	 * Must return a new instance; instances cannot be reused.
	 * 
	 * @return
	 */
	public CostModelComputer createComputer();

	/**
	 * Must return a new instance; instances cannot be reused.
	 * 
	 * @return
	 */
	public CostModelAbstract createCostModel(IvMModel model, IvMLogInfo logInfoFiltered);
}
