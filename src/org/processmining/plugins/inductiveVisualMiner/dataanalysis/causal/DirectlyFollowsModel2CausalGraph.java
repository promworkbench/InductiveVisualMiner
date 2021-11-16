package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class DirectlyFollowsModel2CausalGraph {

	public static final int NO_NODE = -1;
	public static final int START_NODE = -2;
	public static final int END_NODE = -3;

	public static Pair<CausalGraph, CausalDataTable> convert(final DirectlyFollowsModel dfm, IvMLogFiltered log) {
		final TIntObjectMap<TIntSet> node2steps = getNode2StepsMap(dfm);

		StepsGraph stepsGraph = DirectlyFollowsModel2StepsGraph.create(dfm, log, node2steps);
		//		System.out.println(stepsGraph.toDot());

		final TObjectIntMap<TIntSet> steps2rank = StepsGraphRanking.getRanking(stepsGraph);
		//		System.out.println(steps2rank);

		final CausalGraph causalGraph = new CausalGraph();
		final AtomicInteger unfolding = new AtomicInteger(0);
		final List<Choice> traceHistory = new ArrayList<>();

		DirectlyFollowsModelStepsWalk walk = new DirectlyFollowsModelStepsWalk(dfm, node2steps) {
			public void stepsEncountered(TIntSet currentSteps, int chosenNode, TIntSet nextSteps) {
				if (!nextSteps.isEmpty()) {
					Choice currentChoice = getChoice(currentSteps, unfolding.get());
					traceHistory.add(currentChoice);

					if (steps2rank.get(currentSteps) >= steps2rank.get(nextSteps)) {
						/**
						 * This moves back in the ranking of the steps. Move to
						 * the next unfolding.
						 */
						unfolding.getAndIncrement();
					}
					Choice newChoice = getChoice(nextSteps, unfolding.get());

					//System.out.println("  choice decided " + currentChoice + ", next choice " + newChoice);
					for (Choice previousChoice : traceHistory) {
						//add edge to graph
						causalGraph.addEdge(previousChoice, newChoice);
					}
				}
			}
		};

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			unfolding.set(0);
			traceHistory.clear();

			walk.walk(trace);
		}

		//create table
		CausalDataTable table = DirectlyFollowsModel2CausalDataTable.create(dfm, log,
				new ArrayList<>(causalGraph.getNodes()), node2steps, steps2rank);

		return Pair.of(causalGraph, table);
	}

	public static Choice getChoice(TIntSet steps, int unfolding) {
		Choice result = new Choice();
		result.nodes.addAll(steps);
		result.ids.add(unfolding);
		return result;
	}

	public static TIntObjectMap<TIntSet> getNode2StepsMap(DirectlyFollowsModel dfm) {
		TIntObjectMap<TIntSet> result = new TIntObjectHashMap<>(10, 0.5f, NO_NODE);

		TIntSet singularNodes = new TIntHashSet(10, 0.5f, NO_NODE);

		//start node
		{
			TIntSet x = getNextStepsOf(dfm, START_NODE);
			if (x.size() > 1) {
				result.put(START_NODE, x);
			} else {
				singularNodes.add(START_NODE);
			}
		}

		//other nodes
		for (int node : dfm.getNodeIndices()) {
			TIntSet x = getNextStepsOf(dfm, node);
			if (x.size() > 1) {
				result.put(node, x);
			} else {
				singularNodes.add(node);
			}
		}

		//map choices transitively
		for (TIntIterator it = singularNodes.iterator(); it.hasNext();) {
			int node = it.next();
			result.put(node, getNextStepsTransitive(dfm, result, node));
		}

		return result;
	}

	public static TIntSet getNextStepsTransitive(DirectlyFollowsModel dfm, TIntObjectMap<TIntSet> result, int node) {
		if (result.containsKey(node)) {
			return result.get(node);
		}
		if (node == END_NODE) {
			return new TIntHashSet(10, 0.5f, NO_NODE);
		}

		int nextNode;
		if (node == START_NODE) {
			nextNode = dfm.getStartNodes().iterator().next();
		} else {
			nextNode = getNextStepsOf(dfm, node).iterator().next();
		}

		return getNextStepsTransitive(dfm, result, nextNode);
	}

	public static TIntSet getNextStepsOf(DirectlyFollowsModel dfm, int node) {
		TIntSet result = new TIntHashSet(10, 0.5f, NO_NODE);
		if (node == START_NODE) { // start node
			for (TIntIterator it = dfm.getStartNodes().iterator(); it.hasNext();) {
				result.add(it.next());
			}

			if (dfm.isEmptyTraces()) {
				result.add(END_NODE);
			}
		} else if (node == END_NODE) {

		} else {
			for (long edgeIndex : dfm.getEdges()) {
				if (dfm.getEdgeSource(edgeIndex) == node) {
					result.add(dfm.getEdgeTarget(edgeIndex));
				}
			}

			if (dfm.getEndNodes().contains(node)) {
				result.add(END_NODE);
			}
		}
		return result;
	}
}