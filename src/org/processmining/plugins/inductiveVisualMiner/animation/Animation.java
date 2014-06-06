package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class Animation {

	public static List<LocalDotEdge> getEdgesOnMovePath(List<TimedMove> movePath, ShortestPathGraph shortestPath,
			AlignedLogVisualisationInfo info, boolean addSource, boolean addSink) {

		//make a node-path
		List<LocalDotNode> nodePath = new ArrayList<>();
		if (addSource) {
			nodePath.add(info.getSource());
		}
		for (TimedMove move : movePath) {
			LocalDotNode node = getDotNodeFromActivity(move, info);
			if (node != null) {
				nodePath.add(node);
			} else if (move.isModelSync() && move.getUnode().getNode() instanceof Automatic) {
				nodePath.add(info.getModelEdges(move.getUnode()).get(0).getSource());
			}
		}
		if (addSink) {
			nodePath.add(info.getSink());
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

	public static LocalDotEdge getModelMoveEdge(TimedMove move, AlignedLogVisualisationInfo info) {
		List<LocalDotEdge> edges = info.getModelMoveEdges(move.getUnode());
		if (!edges.isEmpty()) {
			return edges.get(0);
		}
		return null;
	}

	public static LocalDotEdge getLogMoveEdge(UnfoldedNode logMoveUnode, UnfoldedNode logMoveBeforeChild,
			AlignedLogVisualisationInfo info) {
		List<LocalDotEdge> edges = info.getLogMoveEdges(logMoveUnode, logMoveBeforeChild);
		if (!edges.isEmpty()) {
			return edges.get(0);
		}
		return null;
	}

	public static LocalDotEdge getTauEdge(TimedMove move, AlignedLogVisualisationInfo info) {
		return info.getModelEdges(move.getUnode()).get(0);
	}

	public static LocalDotNode getParallelSplit(UnfoldedNode unode, AlignedLogVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.parallelSplit) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getParallelJoin(UnfoldedNode unode, AlignedLogVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.parallelJoin) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getDotNodeFromActivity(TimedMove move, AlignedLogVisualisationInfo info) {
		return getDotNodeFromActivity(move.getUnode(), info);
	}
	
	public static LocalDotNode getDotNodeFromActivity(UnfoldedNode unode, AlignedLogVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.activity) {
				return node;
			}
		}
		return null;
	}

	private static void debug(Object s) {
		//		System.out.println(s.toString().replaceAll("\\n", " "));
	}
}
