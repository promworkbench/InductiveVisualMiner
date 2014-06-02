package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class Animation {

	public static List<LocalDotEdge> getEdgesOnMovePath(List<TimedMove> movePath, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel, boolean addSource, boolean addSink) {

		//make a node-path
		List<LocalDotNode> nodePath = new ArrayList<>();
		if (addSource) {
			nodePath.add(panel.getRootSource());
		}
		for (TimedMove move : movePath) {
			LocalDotNode node = getDotNodeFromActivity(move, panel);
			if (node != null) {
				nodePath.add(node);
			} else if (move.isModelSync() && move.getUnode().getNode() instanceof Automatic) {
				nodePath.add(panel.getUnfoldedNode2dotEdgesModel().get(move.getUnode()).get(0).getSource());
			}
		}
		if (addSink) {
			nodePath.add(panel.getRootSink());
		}

		//construct edge-path
		List<LocalDotEdge> result = new ArrayList<>();
		Iterator<LocalDotNode> it = nodePath.iterator();

		if (nodePath.size() < 2) {
			return result;
		}

		LocalDotNode from;
		LocalDotNode to = it.next();

		while (it.hasNext()) {
			from = to;
			to = it.next();

			result.addAll(shortestPath.getShortestPath(from, to));
		}

		return result;
	}

	public static LocalDotEdge getModelMoveEdge(TimedMove move, InductiveVisualMinerPanel panel) {
		return panel.getUnfoldedNode2dotEdgesMove().get(move.getUnode()).get(0);
	}
	
	public static LocalDotEdge getTauEdge(TimedMove move, InductiveVisualMinerPanel panel) {
		return panel.getUnfoldedNode2dotEdgesModel().get(move.getUnode()).get(0);
	}
	
	public static LocalDotNode getParallelSplit(UnfoldedNode unode, InductiveVisualMinerPanel panel) {
		for (LocalDotNode node: panel.getUnfoldedNode2dotNodes().get(unode)) {
			if (node.type == NodeType.parallelSplit) {
				return node;
			}
		}
		return null;
	}
	
	public static LocalDotNode getParallelJoin(UnfoldedNode unode, InductiveVisualMinerPanel panel) {
		for (LocalDotNode node: panel.getUnfoldedNode2dotNodes().get(unode)) {
			if (node.type == NodeType.parallelJoin) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getDotNodeFromActivity(TimedMove move, InductiveVisualMinerPanel panel) {
		if (!panel.getUnfoldedNode2dotNodes().containsKey(move.getUnode())) {
			return null;
		}
		for (LocalDotNode node : panel.getUnfoldedNode2dotNodes().get(move.getUnode())) {
			if (node.type == NodeType.activity) {
				return node;
			}
		}
		return null;
	}

	private static void debug(Object s) {
//		System.out.println(s.toString().replaceAll("\\n", " "));
	}
}
