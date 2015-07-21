package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;

public interface IvMLog extends Iterable<IvMTrace> {
	@Override
	public IteratorWithPosition<IvMTrace> iterator();
}
