package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class CausalAnalysisResult {
	private final TObjectDoubleMap<Pair<Choice, Choice>> edgesCausal = new TObjectDoubleHashMap<Pair<Choice, Choice>>(
			10, 0.5f, -1);

	public void addEdgeCausal(Pair<Choice, Choice> edge, double d) {
		edgesCausal.put(edge, d);
	}

	public List<Pair<Pair<Choice, Choice>, Double>> getResultsCausal() {
		List<Pair<Pair<Choice, Choice>, Double>> result = new ArrayList<>();
		for (Pair<Choice, Choice> edge : edgesCausal.keySet()) {
			result.add(Pair.of(edge, edgesCausal.get(edge)));
		}
		return result;
	}
}
