package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

/**
 * Generic class to perform any chain of computation jobs, which can be ordered
 * as a partially ordered graph.
 * 
 * @author sander
 *
 */
public class Chain {
	private final Graph<ChainLink<?, ?>> graph = GraphFactory.create(ChainLink.class, 13);
	private final InductiveVisualMinerState state;
	private final ProMCanceller globalCanceller;
	private final Executor executor;
	private final OnException onException;
	private Runnable onChange;

	public Chain(InductiveVisualMinerState state, ProMCanceller globalCanceller, Executor executor,
			OnException onException, Runnable onChange) {
		this.state = state;
		this.globalCanceller = globalCanceller;
		this.executor = executor;
		this.onException = onException;
		this.onChange = onChange;
	}

	public void addConnection(ChainLink<?, ?> from, ChainLink<?, ?> to) {
		graph.addEdge(from, to, 1);
	}

	/**
	 * Not thread safe. Only call from the main event thread.
	 * 
	 * @param clazz
	 */
	public synchronized void execute(Class<? extends ChainLink<?, ?>> clazz) {
		//locate the chain link
		ChainLink<?, ?> chainLink = getChainLink(clazz);
		if (chainLink == null) {
			return;
		}

		//invalidate all results that depend on this link
		cancelAndInvalidateResultRecursively(chainLink, state);

		//execute the link
		if (canExecute(chainLink)) {
			try {
				chainLink.execute(globalCanceller, executor, state, this);
			} catch (InterruptedException e) {
				onException.onException(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Thread safe.
	 * 
	 * @param chainLink
	 */
	public synchronized void executeNext(ChainLink<?, ?> chainLink) {
		for (long edge : graph.getOutgoingEdgesOf(chainLink)) {
			ChainLink<?, ?> newChainLink = graph.getEdgeTarget(edge);

			//execute the link
			if (canExecute(newChainLink)) {
				try {
					newChainLink.execute(globalCanceller, executor, state, this);
				} catch (InterruptedException e) {
					onException.onException(e);
					e.printStackTrace();
				}
			}
		}
		onChange.run();
	}

	public boolean canExecute(ChainLink<?, ?> chainLink) {
		for (long edge : graph.getIncomingEdgesOf(chainLink)) {
			if (!graph.getEdgeSource(edge).isComplete()) {
				return false;
			}
		}
		return true;
	}

	private ChainLink<?, ?> getChainLink(Class<? extends ChainLink<?, ?>> clazz) {
		for (ChainLink<?, ?> chainLink : graph.getVertices()) {
			if (clazz.isInstance(chainLink)) {
				return chainLink;
			}
		}
		//assert (false);
		return null;
	}

	private void cancelAndInvalidateResultRecursively(ChainLink<?, ?> chainLink, InductiveVisualMinerState state) {
		chainLink.cancelAndInvalidateResult(state);
		for (long edge : graph.getOutgoingEdgesOf(chainLink)) {
			cancelAndInvalidateResultRecursively(graph.getEdgeTarget(edge), state);
		}
	}

	public Pair<Dot, Map<ChainLink<?, ?>, DotNode>> toDot() {
		Dot result = new Dot();

		Map<ChainLink<?, ?>, DotNode> map = new THashMap<>();
		for (ChainLink<?, ?> vertex : graph.getVertices()) {
			map.put(vertex, result.addNode(vertex.getName()));
		}

		for (long edgeIndex : graph.getEdges()) {
			result.addEdge(map.get(graph.getEdgeSource(edgeIndex)), map.get(graph.getEdgeTarget(edgeIndex)));
		}

		return Pair.of(result, map);
	}

	public Set<ChainLink<?, ?>> getCompletedChainLinks() {
		Set<ChainLink<?, ?>> result = new THashSet<>();
		for (ChainLink<?, ?> vertex : graph.getVertices()) {
			if (vertex.isComplete()) {
				result.add(vertex);
			}
		}
		return result;
	}
}
