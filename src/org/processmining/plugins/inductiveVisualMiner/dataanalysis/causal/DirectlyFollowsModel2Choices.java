package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.Collection;
import java.util.Set;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.DirectlyFollowsModelWalk;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class DirectlyFollowsModel2Choices {
	private Choice currentChoice;
	private int unfolding;
	private Set<TIntList> stepsSeen;

	public Collection<Choice> getChoices(DirectlyFollowsModel dfm, IvMLogFiltered log) {
		final TIntObjectMap<TIntList> node2steps = getNode2StepsMap(dfm);

		DirectlyFollowsModelWalk walk = new DirectlyFollowsModelWalk() {
			public void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex) {
				if (currentChoice.ids.contains(node)) {
					//this event is in the current choice, so it's a new choice
					TIntList steps = node2steps.get(node);
					if (stepsSeen.contains(steps)) {
						/**
						 * This is the second time this unfolding that we
						 * encounter this steps. Move to the next unfolding.
						 */
						//unfol
					}
					Choice newChoice = getChoice(node2steps, node, unfolding);

					currentChoice = newChoice;
				}
			}

			public void nodeEntered(IvMTrace trace, int node, int eventIndex) {

			}

			public void emptyTraceExecuted(IvMTrace trace) {

			}
		};

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			//trace starts with initial choice
			stepsSeen = new THashSet<>();
			unfolding = 0;
			currentChoice = getChoice(node2steps, -1, 1);

			walk.walk(dfm, trace);
		}

		Set<Choice> result = new THashSet<>();

		return result;
	}

	public static Choice getChoice(TIntObjectMap<TIntList> node2steps, int node, int unfolding) {
		Choice result = new Choice();
		result.nodes.addAll(node2steps.get(node));
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