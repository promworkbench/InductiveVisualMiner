package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class CausalAnalysisResult {
	private final TObjectDoubleMap<Pair<Choice, Choice>> edges = new TObjectDoubleHashMap<Pair<Choice, Choice>>(10,
			0.5f, 0);

	public void addEdge(Pair<Choice, Choice> edge, double d) {
		edges.put(edge, d);
	}

	public List<Pair<Pair<Choice, Choice>, Double>> getResults() {
		List<Pair<Pair<Choice, Choice>, Double>> result = new ArrayList<>();
		for (Pair<Choice, Choice> edge : edges.keySet()) {
			result.add(Pair.of(edge, edges.get(edge)));
		}
		return result;
	}
}
