package org.processmining.plugins.inductiveVisualMiner.animation.shortestPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

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
		List<LocalDotEdge> path = DijkstraShortestPath.findPathBetween(graph, from, to);

		if (path == null) {
			return new ArrayList<>();
		}

		return path;
	}
}
