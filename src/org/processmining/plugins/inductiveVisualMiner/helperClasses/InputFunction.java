package org.processmining.plugins.inductiveVisualMiner.helperClasses;


public abstract class InputFunction<I> {
	public abstract void call(I input) throws Exception;
}