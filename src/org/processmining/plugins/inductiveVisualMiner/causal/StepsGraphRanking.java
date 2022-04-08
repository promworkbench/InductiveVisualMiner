package org.processmining.plugins.inductiveVisualMiner.causal;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;

public class StepsGraphRanking {
	public static TObjectIntMap<TIntSet> getRanking(StepsGraph graph) {
		TObjectIntMap<TIntSet> steps2rank = new TObjectIntHashMap<>(10, 0.5f, DirectlyFollowsModel2CausalGraph.NO_NODE);
		int maxRank = 0;

		THashSet<TIntSet> toAddSteps = new THashSet<>(graph.getNodes());

		while (!toAddSteps.isEmpty()) {

			TIntSet steps = findLeastCostSteps(graph, toAddSteps);

			maxRank++;
			steps2rank.put(steps, maxRank);

			toAddSteps.remove(steps);
		}

		return steps2rank;
	}

	private static TIntSet findLeastCostSteps(StepsGraph graph, THashSet<TIntSet> toAddSteps) {
		assert toAddSteps.size() > 0;

		int minCost = Integer.MAX_VALUE;
		TIntSet result = null;
		for (TIntSet steps : toAddSteps) {
			int cost = getCost(graph, toAddSteps, steps);
			if (cost < minCost) {
				minCost = cost;
				result = steps;
			}
		}
		return result;
	}

	private static int getCost(StepsGraph graph, THashSet<TIntSet> toAddSteps, TIntSet steps) {
		int cost = 0;
		for (TIntSet sourceSteps : toAddSteps) {
			if (!sourceSteps.equals(steps)) {
				cost += graph.edges.getCardinalityOf(Pair.of(sourceSteps, steps));
			}
		}
		return cost;
	}
}