package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;

public class IvMLogMetrics {

	public static long getNumberOfTracesRepresented(IvMModel model, int node, IvMLogInfo logInfo)
			throws UnknownTreeNodeException {
		return getNumberOfTracesRepresented(model, node, false, logInfo);
	}

	public static long getNumberOfTracesRepresented(IvMModel model, int from, int to, boolean includeModelMoves,
			IvMLogInfo logInfo) {
		long c = logInfo.getModelEdgeExecutions(from, to);
		if (includeModelMoves) {
			c += logInfo.getModelMoveEdgeExecutions(from, to);
		}
		return c;
	}

	public static long getNumberOfTracesRepresented(IvMModel model, int node, boolean includeModelMoves,
			IvMLogInfo logInfo) throws UnknownTreeNodeException {
		if (model.isTau(node) || model.isActivity(node)) {
			long c = logInfo.getActivities().getCardinalityOf(
					new Move(model, Type.synchronousMove, -2, node, null, null, PerformanceTransition.complete, -100, null));
			if (includeModelMoves) {
				c += getModelMovesLocal(node, logInfo);
			}
			return c;
		} else if (model.isTree()) {
			IvMEfficientTree tree = model.getTree();
			if (tree.isXor(node)) {
				//for the xor itself, there are no transitions fired
				//so, we take the sum of all children
				long result = 0;
				for (int child : tree.getChildren(node)) {
					result += getNumberOfTracesRepresented(model, child, true, logInfo);
				}
				return result;
			} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
				//the sequence has no transitions that can fire
				//pick the maximum of the children
				long result = 0;
				for (int child : tree.getChildren(node)) {
					result = Math.max(result, getNumberOfTracesRepresented(model, child, true, logInfo));
				}
				return result;
			} else if (tree.isLoop(node)) {
				//a loop is executed precisely as often as its exit node.
				//in alignment land, the exit node cannot be skipped
				return getNumberOfTracesRepresented(model, tree.getChild(node, 2), true, logInfo);
			} else if (tree.isOr(node)) {
				//for the OR, there is no way to determine how often it fired just by its children
				//for now, pick the maximum of the children
				return logInfo.getNodeExecutions(tree, node);
			}
		}
		throw new UnknownTreeNodeException();
	}

	public static long getModelMovesLocal(int node, IvMLogInfo logInfo) {
		return logInfo.getModelMoves().get(node);
	}

	public static MultiSet<XEventClass> getLogMoves(LogMovePosition logMovePosition, IvMLogInfo logInfo) {
		if (logInfo.getLogMoves().containsKey(logMovePosition)) {
			return logInfo.getLogMoves().get(logMovePosition);
		}
		return new MultiSet<XEventClass>();
	}

	public static Pair<Long, Long> getExtremes(IvMModel model, IvMLogInfo logInfo) {
		if (model.isTree()) {
			return getExtremesTree(model, logInfo);
		} else {
			return getExtremesDfg(model, logInfo);
		}
	}

	public static Pair<Long, Long> getExtremesTree(IvMModel model, IvMLogInfo logInfo) {
		Pair<Long, Long> p = getExtremesTree(model, model.getTree().getRoot(), logInfo);

		if (model.isActivity(model.getTree().getRoot())) {
			p = Pair.of(Math.min(p.getA(),
					IvMLogMetrics.getNumberOfTracesRepresented(model, model.getTree().getRoot(), false, logInfo)),
					p.getB());
		}

		return p;
	}

	private static Pair<Long, Long> getExtremesTree(IvMModel model, int node, IvMLogInfo logInfo)
			throws UnknownTreeNodeException {
		IvMEfficientTree tree = model.getTree();
		long occurrences = IvMLogMetrics.getNumberOfTracesRepresented(model, node, true, logInfo);
		long modelMoves = IvMLogMetrics.getModelMovesLocal(node, logInfo);
		long min = Math.min(occurrences, modelMoves);
		long max = Math.max(occurrences, modelMoves);

		if (tree.isOperator(node)) {
			for (int child : tree.getChildren(node)) {
				Pair<Long, Long> childResult = getExtremesTree(model, child, logInfo);
				if (min != -1 && childResult.getLeft() != -1) {
					min = Math.min(childResult.getLeft(), min);
				} else if (min == -1) {
					min = childResult.getLeft();
				}
				max = Math.max(childResult.getRight(), max);
			}
		}

		return Pair.of(min, max);
	}

	public static Pair<Long, Long> getExtremesDfg(IvMModel model, IvMLogInfo logInfo) {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		//nodes
		for (int node : model.getAllNodes()) {
			//node itself
			long occurrences = IvMLogMetrics.getNumberOfTracesRepresented(model, node, logInfo);
			min = Math.min(min, occurrences);
			max = Math.max(max, occurrences);

			//start
			occurrences = IvMLogMetrics.getNumberOfTracesRepresented(model, -1, node, true, logInfo);
			min = Math.min(min, occurrences);
			max = Math.max(max, occurrences);

			//end
			occurrences = IvMLogMetrics.getNumberOfTracesRepresented(model, node, -1, true, logInfo);
			min = Math.min(min, occurrences);
			max = Math.max(max, occurrences);
		}
		//edges
		for (long edge : model.getDfg().getEdges()) {
			int from = model.getDfg().getEdgeSource(edge);
			int to = model.getDfg().getEdgeTarget(edge);
			long occurrences = IvMLogMetrics.getNumberOfTracesRepresented(model, from, to, true, logInfo);
			min = Math.min(min, occurrences);
			max = Math.max(max, occurrences);
		}
		//empty traces
		if (model.getDfg().isEmptyTraces()) {
			long occurrences = IvMLogMetrics.getNumberOfTracesRepresented(model, -2, -2, true, logInfo);
			min = Math.min(min, occurrences);
			max = Math.max(max, occurrences);
		}
		return Pair.of(min, max);
	}
}