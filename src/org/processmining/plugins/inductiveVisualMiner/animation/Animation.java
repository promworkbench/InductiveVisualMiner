package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class Animation {

	public static LocalDotEdge getModelMoveEdge(IvMMove move, ProcessTreeVisualisationInfo info) {
		List<LocalDotEdge> edges = info.getModelMoveEdges(move.getTreeNode());
		if (!edges.isEmpty()) {
			return edges.get(0);
		}
		return null;
	}

	public static LocalDotEdge getLogMoveEdge(int logMoveUnode, int logMoveBeforeChild,
			ProcessTreeVisualisationInfo info) {
		return info.getLogMoveEdge(logMoveUnode, logMoveBeforeChild);
	}

	public static LocalDotEdge getTauEdge(IvMMove move, ProcessTreeVisualisationInfo info) {
		return info.getModelEdges(move.getTreeNode()).get(0);
	}

	public static LocalDotNode getParallelSplit(int unode, ProcessTreeVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.concurrentSplit || node.getType() == NodeType.interleavedSplit
					|| node.getType() == NodeType.orSplit) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getParallelJoin(int unode, ProcessTreeVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.concurrentJoin || node.getType() == NodeType.interleavedJoin
					|| node.getType() == NodeType.orJoin) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotNode getDotNodeFromActivity(IvMMove move, ProcessTreeVisualisationInfo info) {
		return getDotNodeFromActivity(move.getTreeNode(), info);
	}

	public static LocalDotNode getDotNodeFromActivity(int unode, ProcessTreeVisualisationInfo info) {
		for (LocalDotNode node : info.getNodes(unode)) {
			if (node.getType() == NodeType.activity) {
				return node;
			}
		}
		return null;
	}

	public static LocalDotEdge getDotEdgeFromLogMove(LogMovePosition logMovePosition, ProcessTreeVisualisationInfo info) {
		for (LocalDotEdge edge : info.getAllLogMoveEdges()) {
			if (logMovePosition.equals(LogMovePosition.of(edge))) {
				return edge;
			}
		}
		return null;
	}

	public static class Input {
		public final IvMEfficientTree tree;
		public final boolean showDeviations;
		public final ShortestPathGraph shortestPath;
		public final ProcessTreeVisualisationInfo info;
		public final Scaler scaler;

		public Input(IvMEfficientTree tree, boolean showDeviations, ShortestPathGraph shortestPath,
				ProcessTreeVisualisationInfo info, Scaler scaler) {
			this.tree = tree;
			this.showDeviations = showDeviations;
			this.shortestPath = shortestPath;
			this.info = info;
			this.scaler = scaler;
		}
	}

	public static class Position {
		public final LocalDotNode dotNode;
		public final Double timestamp;

		public Position(LocalDotNode dotNode, Double timestamp) {
			this.dotNode = dotNode;
			this.timestamp = timestamp;
		}
	}

	public static void moveDotTokenTo(Input in, DotToken dotToken, LocalDotNode destination) {
		List<LocalDotEdge> path = in.shortestPath.getShortestPath(dotToken.getLastPosition(), destination);
		for (LocalDotEdge edge : path) {
			dotToken.addStepOverEdge(edge, null);
		}
	}

	/**
	 * Alter the dotToken to end at the given endPosition.
	 * 
	 * @param in
	 * @param dotToken
	 * @param endPosition
	 */
	public static void moveDotTokenToFinalPosition(Animation.Input in, DotToken dotToken, Position endPosition) {
		List<LocalDotEdge> path = in.shortestPath.getShortestPath(dotToken.getLastPosition(), endPosition.dotNode);
		for (int j = 0; j < path.size() - 1; j++) {
			dotToken.addStepOverEdge(path.get(j), null);
		}
		if (path.size() != 0) {
			dotToken.addStepOverEdge(path.get(path.size() - 1), endPosition.timestamp);
		} else if (endPosition.timestamp != null) {
			//the trace has already ended, fill in the end time
			dotToken.setTimestampOfPoint(dotToken.size() - 1, endPosition.timestamp);
		}
	}
}
