package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class AlignedLogMetrics {

	public static long getNumberOfTracesRepresented(UnfoldedNode unode, AlignedLogInfo logInfo) {
		return getNumberOfTracesRepresented(unode, false, logInfo);
	}
	
	public static long getNumberOfTracesRepresented(UnfoldedNode unode, boolean includeModelMoves, AlignedLogInfo logInfo) {
		if (unode.getNode() instanceof Automatic || unode.getNode() instanceof Manual) {
			long c = logInfo.getActivities().getCardinalityOf(new Move(Type.synchronous, unode, null, false));
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
			}
		}
		assert (false);
		return 0;
	}

	public static long getModelMovesLocal(UnfoldedNode unode, AlignedLogInfo logInfo) {
		return logInfo.getModelMoves().getCardinalityOf(unode);
	}

	public static MultiSet<XEventClass> getLogMoves(LogMovePosition logMovePosition,
			AlignedLogInfo logInfo) {
		if (logInfo.getLogMoves().containsKey(logMovePosition)) {
			return logInfo.getLogMoves().get(logMovePosition);
		}
		return new MultiSet<XEventClass>();
	}

	public static Pair<Long, Long> getExtremes(UnfoldedNode unode, AlignedLogInfo logInfo) {

		long occurrences = AlignedLogMetrics.getNumberOfTracesRepresented(unode, true, logInfo);
		long modelMoves = AlignedLogMetrics.getModelMovesLocal(unode, logInfo);
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
