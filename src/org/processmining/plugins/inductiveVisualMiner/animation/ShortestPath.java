package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotNode;

public class ShortestPath {

	public static List<LocalDotEdge> shortestPath(LocalDotNode from, LocalDotNode to,
			InductiveVisualMinerPanel panel) {
//		System.out.println("   find shortest path " + from + " -> " + to);
		Set<LocalDotNode> nodes = panel.getNodes();
		Map<DotNode, Integer> dist = new HashMap<>();
		Map<LocalDotNode, LocalDotNode> previous = new HashMap<>();
		for (LocalDotNode node : nodes) {
			dist.put(node, Integer.MAX_VALUE);
		}

		dist.put(from, 0);
		Set<LocalDotNode> Q = new HashSet<>();
		Q.addAll(nodes);

		while (!Q.isEmpty()) {
//			System.out.println(dist);
			
			//find node with smallest distance in Q
			LocalDotNode u = null;
			int min = Integer.MAX_VALUE;
			for (LocalDotNode w : Q) {
				if (dist.get(w) <= min) {
					min = dist.get(w);
					u = w;
				}
			}

			Q.remove(u);
			if (dist.get(u) == Integer.MAX_VALUE) {
				break;
			}

			//for each neighbour of u that is still in Q
			for (LocalDotEdge edge : panel.getEdges()) {
				LocalDotNode v = edge.getTarget();
				if (edge.getSource() == u && Q.contains(v)) {
					int alt = dist.get(u) + 1;
					if (alt < dist.get(v)) {
						dist.put(v, alt);
						previous.put(v, u);
					}
				}
			}

		}

		//walk back to find the shortest path
		List<LocalDotNode> result = new ArrayList<>();
		{
			LocalDotNode u = to;
			while (u != null) {
				result.add(0, u);
				u = previous.get(u);
			}
		}

		//transform to edges
		List<LocalDotEdge> result2 = new ArrayList<>();
		{
			Iterator<LocalDotNode> it = result.iterator();
			LocalDotNode u = it.next();
			while (it.hasNext()) {
				LocalDotNode v = it.next();
				result2.add(findEdge(u, v, panel));
				u = v;
			}
		}

		return result2;
	}

	public static LocalDotEdge findEdge(LocalDotNode a, LocalDotNode b, InductiveVisualMinerPanel panel) {
		for (LocalDotEdge e : panel.getEdges()) {
			if (e.getSource() == a && e.getTarget() == b) {
				return e;
			}
		}
		return null;
	}
}
