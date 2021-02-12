package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

/**
 * This is the actual data chain that handles execution of computations and
 * updating of the gui. It is to be accessed using DataChainImplNonBlocking, as
 * this class is not thread safe.
 * 
 * @author sander
 * @param <P>
 *
 */
public class DataChainInternal<P> {
	private boolean debug = false;

	public final DataState state; //public for debug purposes
	private final ProMCanceller globalCanceller;
	private final Executor executor;
	private final InductiveVisualMinerConfiguration configuration;
	private final P panel;
	private final DataChainImplNonBlocking<P> parentChain;

	private final Set<IvMObject<?>> fixedObjects = new THashSet<>();

	private final List<DataChainLink> chainLinks = new ArrayList<>();
	private final Map<IvMObject<?>, Set<DataChainLink>> object2inputs = new THashMap<>();

	/**
	 * Idea: each execution of a chain link has its own canceller. As long as
	 * this canceller is not cancelled, the job is still valid.
	 */
	public final THashMap<DataChainLinkComputation, IvMCanceller> executionCancellers = new THashMap<>(); //public for debug purposes

	public DataChainInternal(DataChainImplNonBlocking<P> parentChain, DataState state, ProMCanceller canceller,
			Executor executor, InductiveVisualMinerConfiguration configuration, P panel) {
		this.state = state;
		this.globalCanceller = canceller;
		this.executor = executor;
		this.configuration = configuration;
		this.panel = panel;
		this.parentChain = parentChain;
	}

	public void register(DataChainLink chainLink) {
		assert !chainLinks.contains(chainLink);

		chainLinks.add(chainLink);

		for (IvMObject<?> object : chainLink.getRequiredObjects()) {
			object2inputs.putIfAbsent(object, new THashSet<DataChainLink>());
			object2inputs.get(object).add(chainLink);
		}
		for (IvMObject<?> object : chainLink.getOptionalObjects()) {
			object2inputs.putIfAbsent(object, new THashSet<DataChainLink>());
			object2inputs.get(object).add(chainLink);
		}
		if (chainLink instanceof DataChainLinkComputation) {
			for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputObjects()) {
				object2inputs.putIfAbsent(object, new THashSet<DataChainLink>());
			}
		}
	}

	public DataChainLink getChainLink(Class<? extends DataChainLink> clazz) {
		for (DataChainLink chainLink : chainLinks) {
			if (clazz.isInstance(chainLink)) {
				return chainLink;
			}
		}
		//assert (false);
		return null;
	}

	/**
	 * Sets an object and starts executing the chain accordingly.
	 */
	public <C> void setObject(IvMObject<C> objectName, C object) {
		if (!fixedObjects.contains(objectName)) {
			state.putObject(objectName, object);

			//start the chain (this will cancel appropriately)
			executeNext(objectName);
		} else {
			if (debug) {
				System.out.println("attempt to set a fixed object: rejected");
			}
		}
	}

	/**
	 * Sets an object such that it cannot be changed anymore. It will execute
	 * the chain for this call, but afterwards this object will not trigger
	 * anything anymore. Chain link computations that have this object as an
	 * output will not be executed.
	 * 
	 * @param object
	 * @param value
	 */
	public <C> void setFixedObject(IvMObject<C> object, C value) {
		state.putObject(object, value);

		//start the chain (this will cancel appropriately)
		executeNext(object);

		fixedObjects.add(object);
	}

	private <C> void executeNext(IvMObject<C> objectBecameAvailable, DataChainLink... exclude) {
		//execute next computation links
		for (DataChainLink chainLink : chainLinks) {
			if (canExecute(chainLink)
					&& (contains(chainLink.getRequiredObjects(), objectBecameAvailable)
							|| contains(chainLink.getOptionalObjects(), objectBecameAvailable))
					&& !contains(exclude, chainLink)) {
				executeLink(chainLink);
			}
		}
	}

	public static boolean contains(DataChainLink[] haystack, DataChainLink needle) {
		for (DataChainLink link : haystack) {
			if (link.getName().equals(needle.getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(IvMObject<?>[] haystack, IvMObject<?> needle) {
		for (IvMObject<?> object : haystack) {
			if (object.equals(needle)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param chainLink
	 * @return whether all the inputs are present
	 */
	private boolean canExecute(DataChainLink chainLink) {
		for (IvMObject<?> inputObject : chainLink.getRequiredObjects()) {
			if (!state.hasObject(inputObject)) {
				return false;
			}
		}
		if (chainLink instanceof DataChainLinkComputation) {
			for (IvMObject<?> outputObject : ((DataChainLinkComputation) chainLink).getOutputObjects()) {
				if (fixedObjects.contains(outputObject)) {
					if (debug) {
						System.out.println("computation not started as it would produce a fixed object");
					}
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void executeLink(DataChainLink chainLink) {
		if (chainLink instanceof DataChainLinkComputation) {
			executeLinkComputation((DataChainLinkComputation) chainLink);
		} else if (chainLink instanceof DataChainLinkGui) {
			executeLinkGui((DataChainLinkGui<P>) chainLink);
		}
		if (parentChain.getOnChange() != null) {
			parentChain.getOnChange().run();
		}
	}

	private void executeLinkGui(final DataChainLinkGui<P> chainLink) {
		//System.out.println("  execute gui chain link `" + chainLink.getName() + "` " + chainLink.getClass());

		final IvMObjectValues inputs = gatherInputs(chainLink);
		if (canExecute(chainLink)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						chainLink.updateGui(panel, inputs);
					} catch (final Exception e) {
						if (parentChain.getOnException() != null) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									parentChain.getOnException().onException(e);
								}
							});
						}
						e.printStackTrace();
						return;
					}
				}
			});
		}
	}

	private IvMObjectValues gatherInputs(DataChainLink chainLink) {
		IvMObjectValues result = new IvMObjectValues();
		for (int i = 0; i < chainLink.getRequiredObjects().length; i++) {
			IvMObject<?> object = chainLink.getRequiredObjects()[i];
			gatherInput(object, result);
		}
		for (int i = 0; i < chainLink.getOptionalObjects().length; i++) {
			IvMObject<?> object = chainLink.getOptionalObjects()[i];
			if (state.hasObject(object)) {
				gatherInput(object, result);
			}
		}
		for (int i = 0; i < chainLink.getNonTriggerObjects().length; i++) {
			IvMObject<?> object = chainLink.getNonTriggerObjects()[i];
			if (state.hasObject(object)) {
				gatherInput(object, result);
			}
		}
		return result;
	}

	private <C> void gatherInput(IvMObject<C> object, IvMObjectValues values) {
		C value = state.getObject(object);
		values.set(object, value);
	}

	private void executeLinkComputation(final DataChainLinkComputation chainLink) {
		//invalidate this link and all links that depend on this link
		cancelLinkAndInvalidateResult(chainLink);

		//execute the link
		if (canExecute(chainLink)) {

			if (debug) {
				System.out.println(
						"  execute computation chain link `" + chainLink.getName() + "` " + chainLink.getClass());
			}

			//set up the canceller for this job
			assert !executionCancellers.containsKey(chainLink);
			final IvMCanceller canceller = new IvMCanceller(globalCanceller);
			executionCancellers.put(chainLink, canceller);

			//gather input
			final IvMObjectValues inputs = gatherInputs(chainLink);

			//set status
			if (parentChain.getOnStatus() != null) {
				parentChain.getOnStatus().startComputation(chainLink);
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
						if (parentChain.getOnException() != null) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									parentChain.getOnException().onException(e);
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
					if (parentChain.getOnStatus() != null) {
						parentChain.getOnStatus().endComputation(chainLink);
					}

					//to process the outputs thread-safely, put them through the parent chain
					parentChain.processOutputsOfChainLink(canceller, chainLink, outputs);
				}
			});
		}
	}

	/**
	 * Invalidate results of this link recursively
	 * 
	 * @param chainLink
	 */
	private void cancelLinkAndInvalidateResult(DataChainLink chainLink) {
		//first, gather things that need to be invalidated
		THashSet<DataChainLink> chainLinksToInvalidate = new THashSet<>();
		THashSet<IvMObject<?>> objectsToInvalidate = new THashSet<>();
		chainLinksToInvalidate.add(chainLink);
		getDownstream(chainLink, chainLinksToInvalidate, objectsToInvalidate);

		//second, invalidate them
		for (DataChainLink chainLink3 : chainLinksToInvalidate) {
			final DataChainLink chainLink2 = chainLink3;

			//cancel the ongoing execution
			if (chainLink2 instanceof DataChainLinkComputation) {
				//cancel the ongoing execution
				IvMCanceller canceller = executionCancellers.get(chainLink2);
				if (canceller != null) {
					canceller.cancel();
					executionCancellers.remove(chainLink2);
				}
			} else if (chainLink2 instanceof DataChainLinkGui) {
				SwingUtilities.invokeLater(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						((DataChainLinkGui<P>) chainLink2).invalidate(panel);
					}
				});
			}
		}

		for (IvMObject<?> object : objectsToInvalidate) {
			state.removeObject(object);
		}
	}

	private void getDownstream(IvMObject<?> object, THashSet<DataChainLink> chainLinksToInvalidate,
			THashSet<IvMObject<?>> objectsToInvalidate) {
		for (DataChainLink chainLink : object2inputs.get(object)) { //includes triggers
			//this chainLink needs this object as its input
			if (chainLinksToInvalidate.add(chainLink)) {
				getDownstream(chainLink, chainLinksToInvalidate, objectsToInvalidate);
			}
		}
	}

	private void getDownstream(DataChainLink chainLink, THashSet<DataChainLink> chainLinksToInvalidate,
			THashSet<IvMObject<?>> objectsToInvalidate) {
		if (chainLink instanceof DataChainLinkComputation) {
			for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputObjects()) {
				if (state.hasObject(object)) {
					if (objectsToInvalidate.add(object)) {
						getDownstream(object, chainLinksToInvalidate, objectsToInvalidate);
					}
				}
			}
		}
	}

	public void processOutputsOfChainLink(IvMCanceller canceller, DataChainLinkComputation chainLink,
			IvMObjectValues outputs) {
		//make sure this computation was not cancelled, and cancel it
		if (canceller.isCancelled()) {
			return;
		}
		canceller.cancel();
		executionCancellers.remove(chainLink);

		if (debug) {
			System.out.println("  chain link `" + chainLink.getName() + "` completed");
		}

		if (outputs != null) {
			for (int i = 0; i < chainLink.getOutputObjects().length; i++) {
				IvMObject<?> outputObjectName = chainLink.getOutputObjects()[i];
				if (!fixedObjects.contains(outputObjectName)) {//if the output object is blocked, do not update it
					processOutput(outputObjectName, outputs);
					executeNext(outputObjectName, chainLink); //trigger chainlinks that have this object as input or trigger, but not the chainlink that produced it itself to prevent loops
				}
			}
		}

		parentChain.getOnChange().run();
	}

	private <C> void processOutput(IvMObject<C> outputObjectName, IvMObjectValues outputs) {
		assert outputs.get(outputObjectName) != null; //check that the declared output is actually present
		state.putObject(outputObjectName, outputs.get(outputObjectName));
	}

	public void getObjectValues(IvMObject<?>[] objects, FutureImpl values) {
		IvMObjectValues result = new IvMObjectValues();
		boolean allPresent = true;
		for (IvMObject<?> object : objects) {
			if (state.hasObject(object)) {
				gatherInput(object, result);
			} else {
				allPresent = false;
			}
		}
		values.set(result, allPresent);
	}

	public Dot toDot() {
		Dot dot = new Dot();

		//add nodes (objects)
		THashMap<IvMObject<?>, DotNode> object2dotNode = new THashMap<>();
		for (IvMObject<?> object : object2inputs.keySet()) {
			DotNode dotNode = dot.addNode(object.getName());
			object2dotNode.put(object, dotNode);
			if (fixedObjects.contains(object)) {
				//fixed object
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "deepskyblue");
			} else if (state.hasObject(object)) {
				//complete
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "aquamarine");
			} else {
				dotNode.setOption("style", "");
			}
		}

		//add nodes (chain links)
		THashMap<DataChainLink, DotNode> link2dotNode = new THashMap<>();
		for (DataChainLink chainLink : chainLinks) {
			DotNode dotNode = dot.addNode(chainLink.getName());
			link2dotNode.put(chainLink, dotNode);
			if (executionCancellers.containsKey(chainLink) && !executionCancellers.get(chainLink).isCancelled()) {
				//busy
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "orange");
			} else {
				dotNode.setOption("style", "");
			}

			if (chainLink instanceof DataChainLinkGui) {
				dotNode.setOption("shape", "box3d");
			} else {
				dotNode.setOption("shape", "box");
			}
		}

		for (DataChainLink chainLink : chainLinks) {
			//inputs
			for (IvMObject<?> object : chainLink.getRequiredObjects()) {
				dot.addEdge(object2dotNode.get(object), link2dotNode.get(chainLink));
			}

			if (chainLink instanceof DataChainLinkComputation) {
				//outputs
				for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputObjects()) {
					dot.addEdge(link2dotNode.get(chainLink), object2dotNode.get(object));
				}

				//triggers
				for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOptionalObjects()) {
					DotEdge edge = dot.addEdge(object2dotNode.get(object), link2dotNode.get(chainLink));
					edge.setOption("style", "dashed");
				}
			}
		}

		return dot;
	}
}