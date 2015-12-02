package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;

/**
 * Defines the basic methods to find out whether a trace is filtered out.
 * 
 * @author sleemans
 *
 */
public interface IvMLogFiltered {
	public boolean isSomethingFiltered();

	public boolean isFilteredOut(int traceIndex);

	/**
	 * 
	 * @return an iterator over all traces that are not filtered out. This
	 *         iterator must support the remove() action, which filters the
	 *         current trace out.
	 */
	public IteratorWithPosition<IvMTrace> iterator();

	/**
	 * 
	 * @return an iterator over all traces, regardless of filtering. This
	 *         iterator must support the remove() action, which filters the
	 *         current trace out.
	 */
	public IteratorWithPosition<IvMTrace> iteratorUnfiltered();
}
