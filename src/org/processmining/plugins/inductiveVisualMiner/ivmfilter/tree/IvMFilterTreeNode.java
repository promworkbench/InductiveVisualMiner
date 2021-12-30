package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

public interface IvMFilterTreeNode<X> {

	public boolean staysInLog(X element);

	/**
	 * A human-readable string explaining which traces will pass this filter,
	 * where each line is indented with indent.
	 * 
	 * @param result
	 * @param indent
	 */
	public void getExplanation(StringBuilder result, int indent);

	/**
	 * 
	 * @return whether at least something could be filtered out. Meant to update
	 *         the explanation of the parent.
	 */
	public boolean couldSomethingBeFiltered();

}