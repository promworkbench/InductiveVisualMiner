package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.Future;

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
 *
 */
public interface DataChain {

	/**
	 * Add a chainlink to the chain
	 * 
	 * @param chainLink
	 */
	public void register(DataChainLink chainLink);

	/**
	 * Sets an object and starts executing the chain accordingly.
	 * 
	 * @param <C>
	 * 
	 * @param inputLog
	 * @param xLog
	 */
	public <C> void setObject(IvMObject<C> objectName, C object);

	/**
	 * Execute an arbitrary chain link of the given class
	 * 
	 * @param clazz
	 */
	public void executeLink(Class<? extends DataChainLink> clazz);

	/**
	 * Invalidate and execute the given chain link.
	 * 
	 * @param chainLink
	 */
	public void executeLink(DataChainLink chainLink);

	/**
	 * Get values from the state if they are available. Objects do not need to
	 * be available before calling this method.
	 * 
	 * @param objects
	 * @return the requested values that are available
	 */
	public Future<IvMObjectValues> getObjectValues(IvMObject<?>[] objects);

	public OnException getOnException();

	public void setOnException(OnException onException);

	public OnStatus getOnStatus();

	public void setOnStatus(OnStatus onStatus);

	public Runnable getOnChange();

	public void setOnChange(Runnable onChange);

	public Dot toDot();

}