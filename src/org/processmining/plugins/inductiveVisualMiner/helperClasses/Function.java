package org.processmining.plugins.inductiveVisualMiner.helperClasses;


public interface Function<I, O> {
	public O call(I input) throws Exception;
}