package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class Animation {

	public static LocalDotEdge getModelMoveEdge(IvMMove move, ProcessTreeVisualisationInfo info) {
		List<LocalDotEdge> edges = info.getModelMoveEdges(move.getUnode());
		if (!edges.isEmpty()) {
			return edges.get(0);
		}
		return null;
	}

	public static LocalDotEdge getLogMoveEdge(UnfoldedNode logMoveUnode, UnfoldedNode logMoveBeforeChild,
			ProcessTreeVisualisationInfo info) {
		return info.getLogMoveEdge(logMoveUnode, logMoveBeforeChild);
	}

	public static LocalDotEdge getTauEdge(IvMMove move, ProcessTreeVisualisationInfo info) {
		return info.getModelEdges(move.getUnode()).get(0);
	}

	public static LocalDotNode getParallelSplit(UnfoldedNode unode, ProcessTreeVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.parallelSplit || node.getType() == NodeType.interleavedSplit) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getParallelJoin(UnfoldedNode unode, ProcessTreeVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.parallelJoin || node.getType() == NodeType.interleavedJoin) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getDotNodeFromActivity(IvMMove move, ProcessTreeVisualisationInfo info) {
		return getDotNodeFromActivity(move.getUnode(), info);
	}
	
	public static LocalDotNode getDotNodeFromActivity(UnfoldedNode unode, ProcessTreeVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.activity) {
				return node;
			}
		}
		return null;
	}
	
	public static LocalDotEdge getDotEdgeFromLogMove(LogMovePosition logMovePosition, ProcessTreeVisualisationInfo info) {
		for (LocalDotEdge edge: info.getAllLogMoveEdges()) {
			if (logMovePosition.equals(LogMovePosition.of(edge))) {
				return edge;
			}
		}
		return null;
	}

}
