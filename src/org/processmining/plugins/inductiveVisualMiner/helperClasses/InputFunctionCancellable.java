package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

public abstract class InputFunctionCancellable<I> {
	public abstract void call(I input, IvMCanceller canceller) throws Exception;
}