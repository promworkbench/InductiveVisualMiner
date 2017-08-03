package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class IvMLogMetrics {

	public static long getNumberOfTracesRepresented(IvMEfficientTree tree, UnfoldedNode unode, IvMLogInfo logInfo)
			throws UnknownTreeNodeException {
		return getNumberOfTracesRepresented(tree, tree.getIndex(unode), false, logInfo);
	}

	public static long getNumberOfTracesRepresented(IvMEfficientTree tree, int node, IvMLogInfo logInfo)
			throws UnknownTreeNodeException {
		return getNumberOfTracesRepresented(tree, node, false, logInfo);
	}

	public static long getNumberOfTracesRepresented(IvMEfficientTree tree, int node, boolean includeModelMoves,
			IvMLogInfo logInfo) throws UnknownTreeNodeException {
		if (tree.isTau(node) || tree.isActivity(node)) {
			long c = logInfo.getActivities().getCardinalityOf(
					new Move(tree, Type.synchronousMove, node, null, null, PerformanceTransition.complete));
			if (includeModelMoves) {
				c += getModelMovesLocal(node, logInfo);
			}
			return c;
		} else if (tree.isXor(node)) {
			//for the xor itself, there are no transitions fired
			//so, we take the sum of all children
			long result = 0;
			for (int child : tree.getChildren(node)) {
				result += getNumberOfTracesRepresented(tree, child, true, logInfo);
			}
			return result;
		} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
			//the sequence has no transitions that can fire
			//pick the maximum of the children
			long result = 0;
			for (int child : tree.getChildren(node)) {
				result = Math.max(result, getNumberOfTracesRepresented(tree, child, true, logInfo));
			}
			return result;
		} else if (tree.isLoop(node)) {
			//a loop is executed precisely as often as its exit node.
			//in alignment land, the exit node cannot be skipped
			return getNumberOfTracesRepresented(tree, tree.getChild(node, 2), true, logInfo);
		} else if (tree.isOr(node)) {
			//for the OR, there is no way to determine how often it fired just by its children
			//for now, pick the maximum of the children
			return logInfo.getNodeExecutions(tree, node);
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

	public static Pair<Long, Long> getExtremes(IvMEfficientTree tree, IvMLogInfo logInfo) {
		Pair<Long, Long> p = getExtremes(tree, tree.getRoot(), logInfo);

		if (tree.isActivity(tree.getRoot())) {
			p = Pair.of(
					Math.min(p.getA(),
							IvMLogMetrics.getNumberOfTracesRepresented(tree, tree.getRoot(), false, logInfo)),
					p.getB());
		}

		return p;
	}

	private static Pair<Long, Long> getExtremes(IvMEfficientTree tree, int node, IvMLogInfo logInfo)
			throws UnknownTreeNodeException {

		long occurrences = IvMLogMetrics.getNumberOfTracesRepresented(tree, node, true, logInfo);
		long modelMoves = IvMLogMetrics.getModelMovesLocal(node, logInfo);
		long min = Math.min(occurrences, modelMoves);
		long max = Math.max(occurrences, modelMoves);

		if (tree.isOperator(node)) {
			for (int child : tree.getChildren(node)) {
				Pair<Long, Long> childResult = getExtremes(tree, child, logInfo);
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
}
