package org.processmining.plugins.inductiveVisualMiner.helperClasses;


public abstract class Function<I, O> {
	public abstract O call(I input) throws Exception;
}