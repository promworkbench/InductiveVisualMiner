package org.processmining.plugins.inductiveVisualMiner.performance;

public interface Performance {

	/**
	 * Returns the asked measure, or -1 if it does not exist.
	 * 
	 * @param type
	 * @param gather
	 * @param node
	 * @return
	 */
	long getNodeMeasure(DurationType type, Aggregate gather, int node);

	long[] getNodeMeasures(DurationType type, Aggregate gather);

	/**
	 * Returns the asked measure, or -1 if it does not exist.
	 * 
	 * @param type
	 * @param gather
	 * @return
	 */
	long getProcessMeasure(DurationType type, Aggregate gather);

	public double getQueueLength(int node, long time);
}