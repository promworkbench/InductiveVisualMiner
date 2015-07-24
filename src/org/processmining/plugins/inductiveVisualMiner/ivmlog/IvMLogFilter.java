package org.processmining.plugins.inductiveVisualMiner.ivmlog;

/**
 * Defines the basic methods to find out whether a trace is filtered out.
 * @author sleemans
 *
 */
public interface IvMLogFilter {
	public boolean isSomethingFiltered();
	public boolean isFilteredOut(int traceIndex);
}
