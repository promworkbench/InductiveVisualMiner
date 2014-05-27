package org.processmining.plugins.inductiveVisualMiner.animation.shortestPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

public class ShortestPathGraph {

	private Map<LocalDotNode, ShortestPathNodeBegin> node2begin = new HashMap<>();
	private Map<LocalDotNode, ShortestPathNodeEnd> node2end = new HashMap<>();
	
	private DefaultDirectedGraph<ShortestPathNode, ShortestPathEdge> graph = new DefaultDirectedGraph<>(ShortestPathEdge.class);
	
	public ShortestPathGraph(Collection<LocalDotNode> nodes, Collection<LocalDotEdge> edges) {
		
		//add all nodes
		for (LocalDotNode node : nodes) {
			ShortestPathNodeBegin begin = new ShortestPathNodeBegin(node);
			ShortestPathNodeEnd end = new ShortestPathNodeEnd(node);
			
			node2begin.put(node, begin);
			node2end.put(node, end);
			
			graph.addVertex(end);
			graph.addVertex(begin);
			graph.addEdge(begin, end, new ShortestPathEdge(null));
		}
		
		//add edges
		for (LocalDotEdge edge: edges) {
			graph.addEdge(node2end.get(edge.getSource()), node2begin.get(edge.getTarget()), new ShortestPathEdge(edge));
		}
	}
	
	public List<LocalDotEdge> getShortestPath(LocalDotNode from, LocalDotNode to) {
		List<ShortestPathEdge> path = DijkstraShortestPath.findPathBetween(graph, node2end.get(from), node2begin.get(to));
		
		List<LocalDotEdge> result = new ArrayList<>();
		if (path == null) {
			return result;
		}
		
		for (ShortestPathEdge edge : path) {
			if (edge.getDotEdge() != null) {
				result.add(edge.getDotEdge());
			}
		}
		
		return result;
		
	}
}
