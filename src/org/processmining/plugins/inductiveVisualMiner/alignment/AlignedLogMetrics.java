package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.metrics.PropertyDirectlyFollowsGraph;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
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

	public static List<UnfoldedNode> unfoldAllNodes(UnfoldedNode unode) {
		List<UnfoldedNode> result = new ArrayList<UnfoldedNode>();
		result.add(unode);
		if (unode.getNode() instanceof Block) {
			for (Node child : unode.getBlock().getChildren()) {
				result.addAll(unfoldAllNodes(unode.unfoldChild(child)));
			}
		}
		return result;
	}

	public static long getNumberOfTracesRepresented(UnfoldedNode unode, AlignedLogInfo logInfo) {
		if (unode.getNode() instanceof Automatic || unode.getNode() instanceof Manual) {
			return logInfo.getActivities().getCardinalityOf(new Move(Type.synchronous, unode));
		} else if (unode.getBlock() != null) {
			if (unode.getBlock() instanceof Xor || unode.getBlock() instanceof Def) {
				//for the xor itself, there are no transitions fired
				//so, we take the sum of all children
				long result = 0;
				for (Node child : unode.getBlock().getChildren()) {
					result += getNumberOfTracesRepresented(unode.unfoldChild(child), logInfo);
				}
				return result;
			} else if (unode.getBlock() instanceof Seq || unode.getBlock() instanceof And) {
				//the sequence has no transitions that can fire
				//pick the maximum of the children
				long result = 0;
				for (Node child : unode.getBlock().getChildren()) {
					result = Math.max(result, getNumberOfTracesRepresented(unode.unfoldChild(child), logInfo));
				}
				return result;
			} else if (unode.getBlock() instanceof XorLoop || unode.getBlock() instanceof DefLoop) {
				//a loop is execute precisely as much times as its exit node.
				//in alignment land, the exit node cannot be skipped
				return getNumberOfTracesRepresented(unode.unfoldChild(unode.getBlock().getChildren().get(2)), logInfo);
			}
		}
		assert (false);
		return 0;
	}

	public static long getNumberOfTimesDfgEdgeTaken(LocalDotEdge edge, AlignedLogInfo dfgLogInfo) {
		//directly-follows edge
		UnfoldedNode source = edge.source.node;
		UnfoldedNode target = edge.target.node;

		if (source.getNode() instanceof Manual && target.getNode() instanceof Manual) {
			//normal dfg-edge
			return dfgLogInfo.getDfg(source, target);
		} else if (source.getNode() instanceof Manual) {
			//end edge
			return dfgLogInfo.getDfg(source, null);
		} else if (target.getNode() instanceof Manual) {
			//start edge
			return dfgLogInfo.getDfg(null, target);
		} else {
			return 0;
		}
	}

	public static long getModelMovesLocal(UnfoldedNode unode, AlignedLogInfo logInfo) {
		return logInfo.getModelMoves().getCardinalityOf(unode);
	}

	public static MultiSet<XEventClass> getLogMoves(UnfoldedNode unode1, UnfoldedNode unode2,
			AlignedLogInfo logInfo) {
		Pair<UnfoldedNode, UnfoldedNode> key = Pair.of(unode1, unode2);
		if (logInfo.getLogMoves().containsKey(key)) {
			return logInfo.getLogMoves().get(key);
		}
		return new MultiSet<XEventClass>();
	}

	public static Pair<Long, Long> getExtremes(UnfoldedNode unode, AlignedLogInfo logInfo, boolean stopAtDfgNode) {

		long occurrences = AlignedLogMetrics.getNumberOfTracesRepresented(unode, logInfo);
		long modelMoves = AlignedLogMetrics.getModelMovesLocal(unode, logInfo);
		long min = Math.min(occurrences, modelMoves);
		long max = Math.max(occurrences, modelMoves);

		if (stopAtDfgNode && PropertyDirectlyFollowsGraph.isSet(unode.getNode())) {
			//we do not want to count taus and xors that are part of a dfg-fallthrough:
			//they are not displayed and occur often
			Pair<Long, Long> childResult = getExtremesDfgNode(unode, logInfo);
			if (min != -1 && childResult.getLeft() != -1) {
				min = Math.min(childResult.getLeft(), min);
			} else if (min == -1) {
				min = childResult.getLeft();
			}
			max = Math.max(childResult.getRight(), max);
		} else if (unode.getNode() instanceof Block) {
			for (Node child : unode.getBlock().getChildren()) {
					Pair<Long, Long> childResult = getExtremes(unode.unfoldChild(child), logInfo, stopAtDfgNode);
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
	
	private static Pair<Long, Long> getExtremesDfgNode(UnfoldedNode unode, AlignedLogInfo logInfo) {
		//expected structure: xorloop(tau, xor(.., ..), tau)
		//or xorloop(tau, a, tau)
		UnfoldedNode xorChild = unode.unfoldChild(unode.getBlock().getChildren().get(1));
		
		if (xorChild.getBlock() == null) {
			//structure is xorloop(tau, a, tau)
			long occurrences = AlignedLogMetrics.getNumberOfTracesRepresented(xorChild, logInfo);
			return Pair.of(occurrences, occurrences);
		}
		
		long min = -1;
		long max = -1; 
		
		for (Node grandChild : xorChild.getBlock().getChildren()) {
			long occurrences = AlignedLogMetrics.getNumberOfTracesRepresented(xorChild.unfoldChild(grandChild), logInfo);
			if (min != -1 && occurrences != -1) {
				min = Math.min(occurrences, min);
			} else if (min == -1) {
				min = occurrences;
			}
			max = Math.max(occurrences, max);
		}
		
		return Pair.of(min, max);
	}

	public static Set<UnfoldedNode> getAllDfgNodes(UnfoldedNode uroot) {
		Set<UnfoldedNode> result = new HashSet<ProcessTree2Petrinet.UnfoldedNode>();
		for (UnfoldedNode unode : AlignedLogMetrics.unfoldAllNodes(uroot)) {
			if (PropertyDirectlyFollowsGraph.isSet(unode.getNode())) {
				result.add(unode);
			}
		}
		return result;
	}
}
