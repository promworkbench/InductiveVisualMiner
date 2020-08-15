package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

import gnu.trove.map.hash.THashMap;

public class ShortestPathGraph {

	private DefaultDirectedGraph<SplitDotNode, SplitDotEdge> graph = new DefaultDirectedGraph<>(SplitDotEdge.class);

	/**
	 * In some cases, the shortest path between two nodes goes through an other
	 * activity. Therefore, we add two nodes for each activity to make the path
	 * through it longer.
	 * 
	 * @author sander
	 *
	 */
	private static class SplitDotNode {

	}

	private static class SplitDotEdge {
		public SplitDotEdge(LocalDotEdge edge) {
			this.edge = edge;
		}

		public SplitDotEdge() {
			edge = null;
		}

		LocalDotEdge edge;
	}

	private Map<SplitDotNode, LocalDotNode> entryNodes = new THashMap<>();
	private Map<LocalDotNode, SplitDotNode> entryNodes2 = new THashMap<>();
	private Map<SplitDotNode, LocalDotNode> exitNodes = new THashMap<>();
	private Map<LocalDotNode, SplitDotNode> exitNodes2 = new THashMap<>();

	public ShortestPathGraph(Collection<LocalDotNode> nodes, Collection<LocalDotEdge> edges) {

		//add all nodes
		for (LocalDotNode node : nodes) {
			if (node.getType() == NodeType.activity) {
				SplitDotNode startNode = new SplitDotNode();
				entryNodes.put(startNode, node);
				entryNodes2.put(node, startNode);
				graph.addVertex(startNode);

				SplitDotNode endNode = new SplitDotNode();
				entryNodes.put(endNode, node);
				exitNodes.put(endNode, node);
				exitNodes2.put(node, endNode);
				graph.addVertex(endNode);

				graph.addEdge(startNode, endNode, new SplitDotEdge());
			} else {
				SplitDotNode splitNode = new SplitDotNode();
				entryNodes.put(splitNode, node);
				entryNodes2.put(node, splitNode);
				exitNodes.put(splitNode, node);
				exitNodes2.put(node, splitNode);
				graph.addVertex(splitNode);
			}
		}

		//add edges
		for (LocalDotEdge edge : edges) {
			LocalDotNode source = edge.getSource();
			LocalDotNode target = edge.getTarget();
			SplitDotNode sourceExit = exitNodes2.get(source);
			SplitDotNode targetEntry = entryNodes2.get(target);
			graph.addEdge(sourceExit, targetEntry, new SplitDotEdge(edge));
		}
	}

	public List<LocalDotEdge> getShortestPath(LocalDotNode from, LocalDotNode to, boolean addSelfEdgeIfNecessary) {
		SplitDotNode fromExit = exitNodes2.get(from);
		SplitDotNode toEntry = entryNodes2.get(to);

		if (from == to) {
			if (graph.containsEdge(fromExit, toEntry)) {
				List<LocalDotEdge> r = new ArrayList<>();

				/*
				 * Difficult one here: if the token flows over a regular edge
				 * from a node to itself, then we should add this edge. However,
				 * if it's a log move, then we should not add it.
				 */
				if (addSelfEdgeIfNecessary && graph.containsEdge(fromExit, toEntry)) {
					r.add(graph.getEdge(fromExit, toEntry).edge);
				}

				return r;
			}
		}

		List<SplitDotEdge> path = DijkstraShortestPath.findPathBetween(graph, fromExit, toEntry);

		if (path == null) {
			throw new RuntimeException("no path found in animation");
		}

		List<LocalDotEdge> result = new ArrayList<>();
		for (SplitDotEdge splitEdge : path) {
			if (splitEdge != null) {
				result.add(splitEdge.edge);
			}
		}

		return result;
	}
}
