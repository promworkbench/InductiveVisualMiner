package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

public interface FunctionCancellable<I, O> {
	public O call(I input, IvMCanceller canceller) throws Exception;
}
