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
	public final THashMap<IvMObject<?>, Set<DataChainLink>> object2inputs = new THashMap<>(); //public for debug purposes

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
		for (IvMObject<?> input : chainLink.getInputNames()) {
			object2inputs.putIfAbsent(input, new THashSet<>());
			object2inputs.get(input).add(chainLink);
		}
	}

	/**
	 * Sets an object and starts executing the chain accordingly.
	 * 
	 * @param <C>
	 * 
	 * @param inputLog
	 * @param xLog
	 */
	public synchronized <C> void setObject(IvMObject<C> objectName, C object) {
		state.putObject(objectName, object);

		//start the chain (this will cancel appropriately)
		executeNext(objectName);
	}

	public synchronized void executeLink(Class<? extends DataChainLink> clazz) {
		//locate the chain link
		DataChainLink chainLink = getChainLink(clazz);
		if (chainLink == null) {
			return;
		}
		executeLink(chainLink);
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
		System.out.println("  execute gui chain link `" + chainLink.getName() + "` " + chainLink.getClass());

		final IvMObjectValues inputs = gatherInputs(chainLink);

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

			System.out.println("  execute computation chain link `" + chainLink.getName() + "` " + chainLink.getClass());

			//set up the canceller for this job
			assert !executionCancellers.containsKey(chainLink);
			final IvMCanceller canceller = new IvMCanceller(globalCanceller);
			executionCancellers.put(chainLink, canceller);

			//gather input
			final IvMObjectValues inputs = gatherInputs(chainLink);

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

					final IvMObjectValues outputs;
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
			IvMObjectValues outputs) {
		//make sure this computation was not cancelled, and cancel it
		if (canceller.isCancelled()) {
			return;
		}
		canceller.cancel();
		executionCancellers.remove(chainLink);

		System.out.println("  chain link `" + chainLink.getName() + "` completed");

		for (int i = 0; i < chainLink.getOutputNames().length; i++) {
			IvMObject<?> outputObjectName = chainLink.getOutputNames()[i];
			processOutput(outputObjectName, outputs);
			executeNext(outputObjectName);
		}

		onChange.run();
	}

	private <C> void processOutput(IvMObject<C> outputObjectName, IvMObjectValues outputs) {
		assert outputs.get(outputObjectName) != null; //check that the declared output is actually present
		state.putObject(outputObjectName, outputs.get(outputObjectName));
	}

	private synchronized <C> void executeNext(IvMObject<C> objectBecameAvailable) {
		//execute next computation links
		if (object2inputs.containsKey(objectBecameAvailable)) {
			for (DataChainLink chainLink : object2inputs.get(objectBecameAvailable)) {
				if (canExecute(chainLink)) {
					executeLink(chainLink);
				}
			}
		}
	}

	private IvMObjectValues gatherInputs(DataChainLink chainLink) {
		IvMObjectValues result = new IvMObjectValues();
		for (int i = 0; i < chainLink.getInputNames().length; i++) {
			IvMObject<?> object = chainLink.getInputNames()[i];
			gatherInput(object, result);
		}
		return result;
	}

	private <C> void gatherInput(IvMObject<C> object, IvMObjectValues values) {
		C value = state.getObject(object);
		values.set(object, value);
	}

	/**
	 * Invalidate results of this link recursively
	 * 
	 * @param chainLink
	 */
	private void cancelLinkAndInvalidateResult(DataChainLink chainLink) {
		//System.out.println("   invalidate chain link " + chainLink.getName());
		if (chainLink instanceof DataChainLinkComputation) {
			//cancel the ongoing execution
			IvMCanceller canceller = executionCancellers.get(chainLink);
			if (canceller != null) {
				canceller.cancel();
				executionCancellers.remove(chainLink);
			}

			//consider all output objects of this computation
			for (IvMObject<?> outputObject : ((DataChainLinkComputation) chainLink).getOutputNames()) {
				state.removeObject(outputObject);

				//recurse on all chain links that use this output object as an input
				if (object2inputs.containsKey(outputObject)) {
					for (DataChainLink chainLink2 : object2inputs.get(outputObject)) {
						cancelLinkAndInvalidateResult(chainLink2);
					}
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
		for (IvMObject<?> inputObject : chainLink.getInputNames()) {
			if (!state.hasObject(inputObject)) {
				return false;
			}
		}
		return true;
	}

	private DataChainLink getChainLink(Class<? extends DataChainLink> clazz) {
		for (Set<DataChainLink> chainLinks : object2inputs.values()) {
			for (DataChainLink chainLink : chainLinks) {
				if (clazz.isInstance(chainLink)) {
					return chainLink;
				}
			}
		}
		//assert (false);
		return null;
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