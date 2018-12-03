package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;

public class ShortestPathGraph {

	private DefaultDirectedGraph<LocalDotNode, LocalDotEdge> graph = new DefaultDirectedGraph<>(LocalDotEdge.class);

	public ShortestPathGraph(Collection<LocalDotNode> nodes, Collection<LocalDotEdge> edges) {

		//add all nodes
		for (LocalDotNode node : nodes) {
			graph.addVertex(node);
		}

		//add edges
		for (LocalDotEdge edge : edges) {
			graph.addEdge(edge.getSource(), edge.getTarget(), edge);
		}
	}

	public List<LocalDotEdge> getShortestPath(LocalDotNode from, LocalDotNode to) {
		
		if (from == to) {
			if (graph.containsEdge(from, to)) {
				List<LocalDotEdge> r = new ArrayList<>();
				r.add(graph.getEdge(from, to));
				return r;
			}
		}
		
		List<LocalDotEdge> path = DijkstraShortestPath.findPathBetween(graph, from, to);

		if (path == null) {
			throw new RuntimeException("no path found in animation");
		}

		return path;
	}
}
