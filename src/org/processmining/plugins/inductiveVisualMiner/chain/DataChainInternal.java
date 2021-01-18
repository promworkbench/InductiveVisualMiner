package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

/**
 * This is the actual data chain that handles execution of computations and
 * updating of the gui. It is to be accessed using DataChainImplNonBlocking, as
 * this class is not thread safe.
 * 
 * @author sander
 *
 */
public class DataChainInternal {
	public final DataState state; //public for debug purposes
	private final ProMCanceller globalCanceller;
	private final Executor executor;
	private final InductiveVisualMinerConfiguration configuration;
	private final InductiveVisualMinerPanel panel;
	private final DataChainImplNonBlocking parentChain;

	private final List<DataChainLink> chainLinks = new ArrayList<>();
	private final Map<IvMObject<?>, Set<DataChainLink>> object2inputs = new THashMap<>();

	/**
	 * Idea: each execution of a chain link has its own canceller. As long as
	 * this canceller is not cancelled, the job is still valid.
	 */
	public final THashMap<DataChainLinkComputation, IvMCanceller> executionCancellers = new THashMap<>(); //public for debug purposes

	public DataChainInternal(DataChainImplNonBlocking parentChain, DataState state, ProMCanceller canceller,
			Executor executor, InductiveVisualMinerConfiguration configuration, InductiveVisualMinerPanel panel) {
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

		for (IvMObject<?> object : chainLink.getInputObjects()) {
			object2inputs.putIfAbsent(object, new THashSet<>());
			object2inputs.get(object).add(chainLink);
		}
		if (chainLink instanceof DataChainLinkComputation) {
			for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputNames()) {
				object2inputs.putIfAbsent(object, new THashSet<>());
			}
			for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getTriggerObjects()) {
				object2inputs.putIfAbsent(object, new THashSet<>());
				object2inputs.get(object).add(chainLink);
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
		state.putObject(objectName, object);

		//start the chain (this will cancel appropriately)
		executeNext(objectName);
	}

	private <C> void executeNext(IvMObject<C> objectBecameAvailable, DataChainLink... exclude) {
		//execute next computation links
		for (DataChainLink chainLink : chainLinks) {
			if (canExecute(chainLink) && contains(chainLink.getInputObjects(), objectBecameAvailable)
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
		for (IvMObject<?> inputObject : chainLink.getInputObjects()) {
			if (!state.hasObject(inputObject)) {
				return false;
			}
		}
		return true;
	}

	public void executeLink(DataChainLink chainLink) {
		if (chainLink instanceof DataChainLinkComputation) {
			executeLinkComputation((DataChainLinkComputation) chainLink);
		} else if (chainLink instanceof DataChainLinkGui) {
			executeLinkGui((DataChainLinkGui) chainLink);
		}
		parentChain.getOnChange().run();
	}

	private void executeLinkGui(final DataChainLinkGui chainLink) {
		//System.out.println("  execute gui chain link `" + chainLink.getName() + "` " + chainLink.getClass());

		final IvMObjectValues inputs = gatherInputs(chainLink);

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

	private IvMObjectValues gatherInputs(DataChainLink chainLink) {
		IvMObjectValues result = new IvMObjectValues();
		for (int i = 0; i < chainLink.getInputObjects().length; i++) {
			IvMObject<?> object = chainLink.getInputObjects()[i];
			gatherInput(object, result);
		}
		return result;
	}

	private <C> void gatherInput(IvMObject<C> object, IvMObjectValues values) {
		C value = state.getObject(object);
		values.set(object, value);
	}

	private void executeLinkComputation(DataChainLinkComputation chainLink) {
		//invalidate this link and all links that depend on this link
		cancelLinkAndInvalidateResult(chainLink);

		//execute the link
		if (canExecute(chainLink)) {

			System.out
					.println("  execute computation chain link `" + chainLink.getName() + "` " + chainLink.getClass());

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

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
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

					if (outputs != null) {
						//to process the outputs thread-safely, put them through the parent chain
						parentChain.processOutputsOfChainLink(canceller, chainLink, outputs);
					}
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
		for (DataChainLink chainLink2 : chainLinksToInvalidate) {
			//cancel the ongoing execution
			if (chainLink2 instanceof DataChainLinkComputation) {
				//cancel the ongoing execution
				IvMCanceller canceller = executionCancellers.get(chainLink2);
				if (canceller != null) {
					canceller.cancel();
					executionCancellers.remove(chainLink2);
				}
			} else if (chainLink instanceof DataChainLinkGui) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						((DataChainLinkGui) chainLink2).invalidate(panel);
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
		for (DataChainLink chainLink : object2inputs.get(object)) {
			//this chainLink needs this object as its input
			if (chainLinksToInvalidate.add(chainLink)) {
				getDownstream(chainLink, chainLinksToInvalidate, objectsToInvalidate);
			}
		}
	}

	private void getDownstream(DataChainLink chainLink, THashSet<DataChainLink> chainLinksToInvalidate,
			THashSet<IvMObject<?>> objectsToInvalidate) {
		if (chainLink instanceof DataChainLinkComputation) {
			for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputNames()) {
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

		System.out.println("  chain link `" + chainLink.getName() + "` completed");

		for (int i = 0; i < chainLink.getOutputNames().length; i++) {
			IvMObject<?> outputObjectName = chainLink.getOutputNames()[i];
			processOutput(outputObjectName, outputs);
			executeNext(outputObjectName, chainLink); //trigger chainlinks that have this object as input, but not the chainlink that produced it itself to prevent loops
		}

		parentChain.getOnChange().run();
	}

	private <C> void processOutput(IvMObject<C> outputObjectName, IvMObjectValues outputs) {
		assert outputs.get(outputObjectName) != null; //check that the declared output is actually present
		state.putObject(outputObjectName, outputs.get(outputObjectName));
	}

	public Dot toDot() {
		Dot dot = new Dot();

		//add nodes (objects)
		THashMap<IvMObject<?>, DotNode> object2dotNode = new THashMap<>();
		for (IvMObject<?> object : object2inputs.keySet()) {
			DotNode dotNode = dot.addNode(object.getName());
			object2dotNode.put(object, dotNode);
			if (state.hasObject(object)) {
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

		//edges (outputs)
		for (DataChainLink chainLink : chainLinks) {
			if (chainLink instanceof DataChainLinkComputation) {
				for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputNames()) {
					dot.addEdge(link2dotNode.get(chainLink), object2dotNode.get(object));
				}
			}
		}

		//edges (inputs)
		for (Entry<IvMObject<?>, Set<DataChainLink>> entry : object2inputs.entrySet()) {
			IvMObject<?> object = entry.getKey();
			for (DataChainLink chainLink : entry.getValue()) {
				dot.addEdge(object2dotNode.get(object), link2dotNode.get(chainLink));
			}
		}

		return dot;
	}

}