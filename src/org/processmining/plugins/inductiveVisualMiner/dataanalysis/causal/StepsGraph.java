package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.Map;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;

public class StepsGraph {
	public MultiSet<Pair<TIntSet, TIntSet>> edges = new MultiSet<>();

	public Set<TIntSet> getNodes() {
		Set<TIntSet> result = new THashSet<>();
		for (Pair<TIntSet, TIntSet> edge : edges) {
			result.add(edge.getA());
			result.add(edge.getB());
		}
		return result;
	}

	public Dot toDot() {
		Dot result = new Dot();

		Map<TIntSet, DotNode> steps2dot = new THashMap<>();
		for (TIntSet steps : getNodes()) {
			DotNode dotNode = result.addNode(steps.toString());
			steps2dot.put(steps, dotNode);
		}

		for (Pair<TIntSet, TIntSet> edge : edges) {
			DotNode dotNodeA = steps2dot.get(edge.getA());
			DotNode dotNodeB = steps2dot.get(edge.getB());
			assert dotNodeA != null && dotNodeB != null;
			result.addEdge(dotNodeA, dotNodeB, edges.getCardinalityOf(edge) + "");
		}

		return result;
	}
}