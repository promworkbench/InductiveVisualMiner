package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

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
public class DataChain {

	/*
	 * Which object is required by which chain link?
	 */
	public final THashMap<String, Set<DataChainLink>> object2inputs = new THashMap<>(); //public for debug purposes

	/**
	 * Idea: each execution of a chain link has its own canceller. As long as
	 * this canceller is not cancelled, the job is still valid.
	 */
	public final THashMap<DataChainLinkComputation, IvMCanceller> executionCancellers = new THashMap<>(); //public for debug purposes

	public final DataState state; //public for debug purposes
	private final ProMCanceller globalCanceller;
	private final Executor executor;
	private final InductiveVisualMinerConfiguration configuration;
	private final InductiveVisualMinerPanel panel;

	private OnException onException;
	private Runnable onChange;
	private OnStatus onStatus;

	public DataChain(DataState state, ProMCanceller canceller, Executor executor,
			InductiveVisualMinerConfiguration configuration, InductiveVisualMinerPanel panel) {
		this.state = state;
		this.globalCanceller = canceller;
		this.executor = executor;
		this.configuration = configuration;
		this.panel = panel;
	}

	/**
	 * Add a chainlink to the chain
	 * 
	 * @param chainLink
	 */
	public synchronized void register(DataChainLink chainLink) {
		for (String input : chainLink.getInputNames()) {
			object2inputs.putIfAbsent(input, new THashSet<>());
			object2inputs.get(input).add(chainLink);
		}
	}

	/**
	 * Sets an object and starts executing the chain accordingly.
	 * 
	 * @param inputLog
	 * @param xLog
	 */
	public synchronized void setObject(String objectName, Object object) {
		state.putObject(objectName, object);

		//start the chain (this will cancel appropriately)
		executeNext(objectName);
	}

	public synchronized void executeLink(DataChainLink chainLink) {
		if (chainLink instanceof DataChainLinkComputation) {
			executeLinkComputation((DataChainLinkComputation) chainLink);
		} else if (chainLink instanceof DataChainLinkGui) {
			executeLinkGui((DataChainLinkGui) chainLink);
		}
		onChange.run();
	}

	private synchronized void executeLinkGui(final DataChainLinkGui chainLink) {
		final Object[] inputs = gatherInputs(chainLink);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					chainLink.updateGui(panel, inputs);
				} catch (final Exception e) {
					if (getOnException() != null) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								getOnException().onException(e);
							}
						});
					}
					e.printStackTrace();
					return;
				}
			}
		});
	}

	private synchronized void executeLinkComputation(DataChainLinkComputation chainLink) {
		//invalidate this link and all links that depend on this link
		cancelLinkAndInvalidateResult(chainLink);

		//execute the link
		if (canExecute(chainLink)) {

			//set up the canceller for this job
			assert !executionCancellers.containsKey(chainLink);
			final IvMCanceller canceller = new IvMCanceller(globalCanceller);
			executionCancellers.put(chainLink, canceller);

			//gather input
			final Object[] inputs = gatherInputs(chainLink);

			//set status
			if (onStatus != null) {
				onStatus.startComputation(chainLink);
			}

			//execute (in own thread)
			executor.execute(new Runnable() {
				public void run() {
					if (canceller.isCancelled()) {
						return;
					}

					final Object[] outputs;
					try {
						outputs = chainLink.execute(configuration, inputs, canceller);
					} catch (final Exception e) {
						if (getOnException() != null) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									getOnException().onException(e);
								}
							});
						}
						e.printStackTrace();
						return;
					}

					if (canceller.isCancelled()) {
						return;
					}

					//set status
					if (onStatus != null) {
						onStatus.endComputation(chainLink);
					}

					processOutputsOfChainLink(canceller, chainLink, outputs);
				}
			});
		}
	}

	private synchronized void processOutputsOfChainLink(IvMCanceller canceller, DataChainLinkComputation chainLink,
			Object[] outputs) {
		//make sure this computation was not cancelled, and cancel it
		if (canceller.isCancelled()) {
			return;
		}
		canceller.cancel();
		executionCancellers.remove(chainLink);

		for (int i = 0; i < outputs.length; i++) {
			String outputObjectName = chainLink.getOutputNames()[i];
			state.putObject(outputObjectName, outputs[i]);

			executeNext(outputObjectName);
		}
	}

	private synchronized void executeNext(String objectBecameAvailable) {
		//execute next computation links
		for (DataChainLink chainLink : object2inputs.get(objectBecameAvailable)) {
			if (canExecute(chainLink)) {
				executeLink(chainLink);
			}
		}
	}

	private Object[] gatherInputs(DataChainLink chainLink) {
		Object[] result = new Object[chainLink.getInputNames().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = state.getObject(chainLink.getInputNames()[i]);
		}
		return result;
	}

	/**
	 * Invalidate results of this link recursively
	 * 
	 * @param chainLink
	 */
	private void cancelLinkAndInvalidateResult(DataChainLink chainLink) {
		if (chainLink instanceof DataChainLinkComputation) {
			//cancel the ongoing execution
			IvMCanceller canceller = executionCancellers.get(chainLink);
			if (canceller != null) {
				canceller.cancel();
				executionCancellers.remove(chainLink);
			}

			//consider all output objects of this computation
			for (String outputObject : ((DataChainLinkComputation) chainLink).getOutputNames()) {
				state.removeObject(outputObject);

				//recurse on all chain links that use this output object as an input
				for (DataChainLink chainLink2 : object2inputs.get(outputObject)) {
					cancelLinkAndInvalidateResult(chainLink2);
				}
			}
		} else if (chainLink instanceof DataChainLinkGui) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((DataChainLinkGui) chainLink).invalidate(panel);
				}
			});
		}
	}

	/**
	 * 
	 * @param chainLink
	 * @return whether all the inputs are present
	 */
	private boolean canExecute(DataChainLink chainLink) {
		for (String inputObject : chainLink.getInputNames()) {
			if (!state.hasObject(inputObject)) {
				return false;
			}
		}
		return true;
	}

	public OnException getOnException() {
		return onException;
	}

	public void setOnException(OnException onException) {
		this.onException = onException;
	}

	public OnStatus getOnStatus() {
		return onStatus;
	}

	public void setOnStatus(OnStatus onStatus) {
		this.onStatus = onStatus;
	}

	public Runnable getOnChange() {
		return onChange;
	}

	public void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}
}