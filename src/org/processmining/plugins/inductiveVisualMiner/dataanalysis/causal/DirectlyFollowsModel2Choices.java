package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.concurrent.atomic.AtomicInteger;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class DirectlyFollowsModel2Choices {

	public static Dot getChoices(final DirectlyFollowsModel dfm, IvMLogFiltered log) {
		final TIntObjectMap<TIntList> node2steps = getNode2StepsMap(dfm);
		final THashMap<Choice, DotNode> choice2dotNode = new THashMap<>();
		final Dot dot = new Dot();
		final THashSet<Pair<Choice, Choice>> edges = new THashSet<>();
		final AtomicInteger unfolding = new AtomicInteger(0);

		final TObjectIntMap<TIntList> steps2rank = getRank(dfm, node2steps, log);

		DirectlyFollowsModelStepsWalk walk = new DirectlyFollowsModelStepsWalk(dfm, node2steps) {
			public void stepsEncountered(TIntList steps, int chosenNode, TIntList nextSteps) {
				Choice currentChoice = getChoice(steps, unfolding.get());

				if (steps2rank.get(steps) >= steps2rank.get(nextSteps)) {
					/**
					 * This moves back in the ranking of the steps. Move to the
					 * next unfolding.
					 */
					unfolding.getAndIncrement();
				}
				Choice newChoice = getChoice(nextSteps, unfolding.get());

				System.out.println("  choice decided " + currentChoice + ", next choice " + newChoice);

				//add node and edge to graph
				{
					DotNode newDotNode = choice2dotNode.get(newChoice);
					if (newDotNode == null) {
						newDotNode = dot.addNode(newChoice.getId());
						choice2dotNode.put(newChoice, newDotNode);
					}
					DotNode currentDotNode = choice2dotNode.get(currentChoice);
					if (currentDotNode == null) {
						currentDotNode = dot.addNode(currentChoice.getId());
						choice2dotNode.put(currentChoice, currentDotNode);
					}
					assert newDotNode != null && currentDotNode != null;
					if (edges.add(Pair.of(currentChoice, newChoice))) {
						dot.addEdge(currentDotNode, newDotNode);
					}
				}
			}
		};

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			//trace starts with initial choice
			unfolding.set(0);

			walk.walk(trace);
		}

		System.out.println(dot.toString());
		return dot;
	}

	public static TObjectIntMap<TIntList> getRank(DirectlyFollowsModel dfm, TIntObjectMap<TIntList> node2steps,
			IvMLogFiltered log) {
		TObjectIntMap<TIntList> steps2rank = new TObjectIntHashMap<>(10, 0.5f, -1);
		int maxRank = 0;
		THashSet<TIntList> coveredSteps = new THashSet<>();

		//first, count how often each edge appears
		final MultiSet<Pair<TIntList, TIntList>> edges = new MultiSet<>();
		{
			DirectlyFollowsModelStepsWalk walk = new DirectlyFollowsModelStepsWalk(dfm, node2steps) {
				public void stepsEncountered(TIntList steps, int chosenNode, TIntList nextSteps) {
					edges.add(Pair.of(steps, nextSteps));
				}
			};

			for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
				walk.walk(it.next());
			}
		}

		//the initial choice must of course be the first ranked steps
		{
			TIntList initSteps = node2steps.get(-1);
			maxRank++;
			steps2rank.put(initSteps, maxRank);
			coveredSteps.add(initSteps);
		}

		//second, find a minimum spanning tree
		{
			//sort by cardinality
			for (Pair<TIntList, TIntList> p : edges.sortByCardinality()) {
				TIntList stepsA = p.getA();
				TIntList stepsB = p.getB();

				if (!coveredSteps.contains(stepsA)) {
					coveredSteps.add(stepsA);
					maxRank++;
					steps2rank.put(stepsA, maxRank);
				}
				if (!coveredSteps.contains(stepsB)) {
					coveredSteps.add(stepsA);
					maxRank++;
					steps2rank.put(stepsA, maxRank);
				}
			}
		}

		return steps2rank;
	}

	public static Choice getChoice(TIntList steps, int unfolding) {
		Choice result = new Choice();
		result.nodes.addAll(steps);
		result.ids.add(unfolding);
		return result;
	}

	public static TIntObjectMap<TIntList> getNode2StepsMap(DirectlyFollowsModel dfm) {
		TIntObjectMap<TIntList> result = new TIntObjectHashMap<TIntList>(10, 0.5f, -1);

		TIntList singularNodes = new TIntArrayList();

		//start node
		{
			TIntList x = getNextStepsOf(dfm, -1);
			if (x.size() > 1) {
				result.put(-1, x);
			} else {
				singularNodes.add(-1);
			}
		}

		//other nodes
		for (int node : dfm.getNodeIndices()) {
			TIntList x = getNextStepsOf(dfm, node);
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

	public static TIntList getNextStepsTransitive(DirectlyFollowsModel dfm, TIntObjectMap<TIntList> node2choice,
			int node) {
		if (node2choice.containsKey(node)) {
			return node2choice.get(node);
		}

		int nextNode;
		if (node == -1) {
			nextNode = dfm.getStartNodes().iterator().next();
		} else {
			nextNode = getNextStepsOf(dfm, node).iterator().next();
		}

		return getNextStepsTransitive(dfm, node2choice, nextNode);
	}

	public static TIntList getNextStepsOf(DirectlyFollowsModel dfm, int node) {
		TIntList result = new TIntArrayList();
		if (node == -1) {
			for (TIntIterator it = dfm.getStartNodes().iterator(); it.hasNext();) {
				result.add(it.next());
			}

			if (dfm.isEmptyTraces()) {
				result.add(-1);
			}
		} else {
			for (long edgeIndex : dfm.getEdges()) {
				if (dfm.getEdgeSource(edgeIndex) == node) {
					result.add(dfm.getEdgeTarget(edgeIndex));
				}
			}

			if (dfm.getEndNodes().contains(node)) {
				result.add(-1);
			}
		}
		return result;
	}
}