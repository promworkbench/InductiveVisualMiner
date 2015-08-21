package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Or;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class IvMLogMetrics {

	public static long getNumberOfTracesRepresented(UnfoldedNode unode, IvMLogInfo logInfo) {
		return getNumberOfTracesRepresented(unode, false, logInfo);
	}
	
	public static long getNumberOfTracesRepresented(UnfoldedNode unode, boolean includeModelMoves, IvMLogInfo logInfo) {
		if (unode.getNode() instanceof Automatic || unode.getNode() instanceof Manual) {
			long c = logInfo.getActivities().getCardinalityOf(new Move(Type.synchronousMove, unode, null, null, PerformanceTransition.complete));
			if (includeModelMoves) {
				c += getModelMovesLocal(unode, logInfo);
			}
			return c;
		} else if (unode.getBlock() != null) {
			if (unode.getBlock() instanceof Xor || unode.getBlock() instanceof Def) {
				//for the xor itself, there are no transitions fired
				//so, we take the sum of all children
				long result = 0;
				for (Node child : unode.getBlock().getChildren()) {
					result += getNumberOfTracesRepresented(unode.unfoldChild(child), true, logInfo);
				}
				return result;
			} else if (unode.getBlock() instanceof Seq || unode.getBlock() instanceof And) {
				//the sequence has no transitions that can fire
				//pick the maximum of the children
				long result = 0;
				for (Node child : unode.getBlock().getChildren()) {
					result = Math.max(result, getNumberOfTracesRepresented(unode.unfoldChild(child), true, logInfo));
				}
				return result;
			} else if (unode.getBlock() instanceof XorLoop || unode.getBlock() instanceof DefLoop) {
				//a loop is executed precisely as often as its exit node.
				//in alignment land, the exit node cannot be skipped
				return getNumberOfTracesRepresented(unode.unfoldChild(unode.getBlock().getChildren().get(2)), true, logInfo);
			} else if (unode.getBlock() instanceof Or) {
				//for the OR, there is no way to determine how often it fired just by its children
				//for now, pick the maximum of the children
				//TODO: find better way
				long result = 0;
				for (Node child : unode.getBlock().getChildren()) {
					result = Math.max(result, getNumberOfTracesRepresented(unode.unfoldChild(child), true, logInfo));
				}
				return result;
			}
		}
		assert (false);
		return 0;
	}

	public static long getModelMovesLocal(UnfoldedNode unode, IvMLogInfo logInfo) {
		return logInfo.getModelMoves().getCardinalityOf(unode);
	}

	public static MultiSet<XEventClass> getLogMoves(LogMovePosition logMovePosition,
			IvMLogInfo logInfo) {
		if (logInfo.getLogMoves().containsKey(logMovePosition)) {
			return logInfo.getLogMoves().get(logMovePosition);
		}
		return new MultiSet<XEventClass>();
	}

	public static Pair<Long, Long> getExtremes(UnfoldedNode unode, IvMLogInfo logInfo) {

		long occurrences = IvMLogMetrics.getNumberOfTracesRepresented(unode, true, logInfo);
		long modelMoves = IvMLogMetrics.getModelMovesLocal(unode, logInfo);
		long min = Math.min(occurrences, modelMoves);
		long max = Math.max(occurrences, modelMoves);

		if (unode.getNode() instanceof Block) {
			for (Node child : unode.getBlock().getChildren()) {
					Pair<Long, Long> childResult = getExtremes(unode.unfoldChild(child), logInfo);
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
