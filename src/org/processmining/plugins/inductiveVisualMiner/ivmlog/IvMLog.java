package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;

/**
 * Provides an iterator over traces.
 * 
 * @author sleemans
 *
 */
public interface IvMLog extends Iterable<IvMTrace> {
	@Override
	public IteratorWithPosition<IvMTrace> iterator();
}
