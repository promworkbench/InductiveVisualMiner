package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

/**
 * Generic class to perform any chain of computation jobs, which can be ordered
 * as a partially ordered graph.
 * 
 * @author sander
 *
 */
public class Chain<State> {
	private final Graph<ChainLink<State, ?, ?>> graph = GraphFactory.create(ChainLink.class, 13);
	private final State state;
	private final ProMCanceller globalCanceller;
	private final Executor executor;
	private Runnable onChange;

	public Runnable getOnChange() {
		return onChange;
	}

	public void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}

	public Chain(State state, ProMCanceller globalCanceller, Executor executor) {
		this.state = state;
		this.globalCanceller = globalCanceller;
		this.executor = executor;
	}

	public void addConnection(ChainLink<State, ?, ?> from, ChainLink<State, ?, ?> to) {
		graph.addEdge(from, to, 1);
	}

	/**
	 * Not thread safe. Only call from the main event thread.
	 * 
	 * @param clazz
	 */
	public synchronized void execute(Class<? extends ChainLink<State, ?, ?>> clazz) {
		//locate the chain link
		ChainLink<State, ?, ?> chainLink = getChainLink(clazz);
		if (chainLink == null) {
			return;
		}

		//invalidate all results that depend on this link
		cancelAndInvalidateResultRecursively(chainLink, state);

		//execute the link
		if (canExecute(chainLink)) {
			chainLink.execute(globalCanceller, executor, state, this);
		}
	}

	/**
	 * Thread safe.
	 * 
	 * @param chainLink
	 */
	public synchronized void executeNext(ChainLink<State, ?, ?> chainLink) {
		for (long edge : graph.getOutgoingEdgesOf(chainLink)) {
			ChainLink<State, ?, ?> newChainLink = graph.getEdgeTarget(edge);

			//execute the link
			if (canExecute(newChainLink)) {
				newChainLink.execute(globalCanceller, executor, state, this);
			}
		}
		if (onChange != null) {
			onChange.run();
		}
	}

	public boolean canExecute(ChainLink<State, ?, ?> chainLink) {
		for (long edge : graph.getIncomingEdgesOf(chainLink)) {
			if (!graph.getEdgeSource(edge).isComplete()) {
				return false;
			}
		}
		return true;
	}

	private ChainLink<State, ?, ?> getChainLink(Class<? extends ChainLink<State, ?, ?>> clazz) {
		for (ChainLink<State, ?, ?> chainLink : graph.getVertices()) {
			if (clazz.isInstance(chainLink)) {
				return chainLink;
			}
		}
		//assert (false);
		return null;
	}

	private void cancelAndInvalidateResultRecursively(ChainLink<State, ?, ?> chainLink, State state) {
		chainLink.cancelAndInvalidateResult(state);
		for (long edge : graph.getOutgoingEdgesOf(chainLink)) {
			cancelAndInvalidateResultRecursively(graph.getEdgeTarget(edge), state);
		}
	}

	public Pair<Dot, Map<ChainLink<State, ?, ?>, DotNode>> toDot() {
		Dot result = new Dot();

		Map<ChainLink<State, ?, ?>, DotNode> map = new THashMap<>();
		for (ChainLink<State, ?, ?> vertex : graph.getVertices()) {
			map.put(vertex, result.addNode(vertex.getName()));
		}

		for (long edgeIndex : graph.getEdges()) {
			result.addEdge(map.get(graph.getEdgeSource(edgeIndex)), map.get(graph.getEdgeTarget(edgeIndex)));
		}

		return Pair.of(result, map);
	}

	public Collection<ChainLink<State, ?, ?>> getChainLinks() {
		return Collections.unmodifiableCollection(Arrays.asList(graph.getVertices()));
	}

	public Set<ChainLink<State, ?, ?>> getCompletedChainLinks() {
		Set<ChainLink<State, ?, ?>> result = new THashSet<>();
		for (ChainLink<State, ?, ?> vertex : graph.getVertices()) {
			if (vertex.isComplete()) {
				result.add(vertex);
			}
		}
		return result;
	}
}
