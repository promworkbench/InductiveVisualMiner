package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.graphviz.dot.Dot;

/**
 * Idea: a chain of execution steps that works by data objects: chainlinks
 * declare their inputs and outputs, and are started as soon as their outputs
 * are available.
 * 
 * This will enable a cleaner separation between GUI and computation steps, as
 * the GUI steps are now chainlinks as well, but executed in the GUI thread.
 * 
 * Each chainlink is started whenever all its inputs have become available.
 * 
 * @author sander
 * @param <C>
 *
 */
public interface DataChain<C> {

	/**
	 * Add a chainlink to the chain
	 * 
	 * @param chainLink
	 */
	public void register(DataChainLink<C> chainLink);

	/**
	 * Sets an object and starts executing the chain accordingly.
	 * 
	 * @param <O>
	 * 
	 * @param inputLog
	 * @param xLog
	 */
	public <O> void setObject(IvMObject<O> objectName, O object);

	/**
	 * Execute an arbitrary chain link of the given class
	 * 
	 * @param clazz
	 */
	public void executeLink(Class<? extends DataChainLink<C>> clazz);

	/**
	 * Invalidate and execute the given chain link.
	 * 
	 * @param chainLink
	 */
	public void executeLink(DataChainLink<C> chainLink);

	/**
	 * Get values from the state if they are available. Objects do not need to
	 * be available before calling this method.
	 * 
	 * @param objects
	 * @return the requested values that are available
	 */
	public FutureImpl getObjectValues(IvMObject<?>... objects);

	/**
	 * Set an object that cannot be changed by the chain. Any attempt to do so
	 * will be ignored silently. Triggers will be suppressed after this call
	 * (the call itself will trigger for the object).
	 * 
	 * @param <O>
	 * 
	 * @param object
	 * @param value
	 */
	public <O> void setFixedObject(IvMObject<O> object, O value);

	public OnException getOnException();

	public void setOnException(OnException onException);

	public OnStatus<C> getOnStatus();

	public void setOnStatus(OnStatus<C> onStatus);

	public Runnable getOnChange();

	public void setOnChange(Runnable onChange);

	public Dot toDot();
}