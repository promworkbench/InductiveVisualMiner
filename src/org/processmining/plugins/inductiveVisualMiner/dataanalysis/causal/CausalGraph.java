package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.Collections;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class CausalGraph {
	private final THashSet<Pair<Choice, Choice>> edges = new THashSet<>();
	private final THashSet<Choice> nodes = new THashSet<>();

	public void addEdge(Choice sourceChoice, Choice targetChoice) {
		nodes.add(sourceChoice);
		nodes.add(targetChoice);
		edges.add(Pair.of(sourceChoice, targetChoice));
	}

	public Set<Pair<Choice, Choice>> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	public Set<Choice> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}

	public Dot toDot() {
		Dot dot = new Dot();

		//nodes
		THashMap<Choice, DotNode> choice2dotNode = new THashMap<>();
		for (Choice choice : nodes) {
			DotNode dotNode = dot.addNode(choice.getId());
			choice2dotNode.put(choice, dotNode);
		}

		//edges
		for (Pair<Choice, Choice> p : getEdges()) {
			DotNode sourceDotNode = choice2dotNode.get(p.getA());
			DotNode targetDotNode = choice2dotNode.get(p.getB());
			assert targetDotNode != null && sourceDotNode != null;
			dot.addEdge(sourceDotNode, targetDotNode);
		}

		return dot;
	}
}