package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;

public class AlignedLogSplitter {

	//get the log of a particular node
	public static AlignedLog getLog(UnfoldedNode unode, AlignedLog log) {
		return getLogRecursive(unode.getPath().subList(1, unode.getPath().size()), new UnfoldedNode(unode.getNode()
				.getProcessTree().getRoot()), log);
	}

	private static AlignedLog getLogRecursive(List<Node> path, UnfoldedNode unode, AlignedLog log) {
		if (path.size() == 0) {
			return log;
		}

		UnfoldedNode nextChild = unode.unfoldChild(path.get(0));
		int childIndex = unode.getBlock().getChildren().indexOf(nextChild.getNode());

		//make a partition
		List<Set<UnfoldedNode>> partition = new LinkedList<Set<UnfoldedNode>>();
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uchild = unode.unfoldChild(child);
			partition.add(new HashSet<UnfoldedNode>(AlignedLogMetrics.unfoldAllNodes(uchild)));
		}

		//split the log
		List<AlignedLog> sublogs = split(log, partition, unode.getBlock() instanceof XorLoop);

		//find the correct sublog
		AlignedLog sublog = sublogs.get(childIndex);

		AlignedLog result = getLogRecursive(path.subList(1, path.size()), nextChild, sublog);
		return result;
	}

	//split a log using an alignment
	public static List<AlignedLog> split(AlignedLog log, List<Set<UnfoldedNode>> partition, boolean loop) {
		List<AlignedLog> result = new LinkedList<AlignedLog>();

		//map activities to sigmas
		HashMap<Set<UnfoldedNode>, AlignedLog> mapSigma2sublog = new HashMap<Set<UnfoldedNode>, AlignedLog>();
		HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma = new HashMap<UnfoldedNode, Set<UnfoldedNode>>();
		for (Set<UnfoldedNode> sigma : partition) {
			AlignedLog sublog = new AlignedLog();
			result.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (UnfoldedNode activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}

		for (IMTraceG<Move> trace : log) {
			if (!loop) {
				splitParallel(result, trace, partition, log.getCardinalityOf(trace), mapSigma2sublog, mapActivity2sigma);
			} else {
				splitLoop(result, trace, partition, log.getCardinalityOf(trace), mapSigma2sublog, mapActivity2sigma);
			}
		}

		return result;
	}

	public static void splitParallel(List<AlignedLog> result, IMTraceG<Move> trace, List<Set<UnfoldedNode>> partition,
			long cardinality, HashMap<Set<UnfoldedNode>, AlignedLog> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma) {

		//add a new trace to every sublog
		HashMap<Set<UnfoldedNode>, IMTraceG<Move>> mapSigma2subtrace = new HashMap<Set<UnfoldedNode>, IMTraceG<Move>>();
		for (Set<UnfoldedNode> sigma : partition) {
			IMTraceG<Move> subtrace = new IMTraceG<Move>();
			mapSigma2subtrace.put(sigma, subtrace);
		}

		for (Move move : trace) {
			if (move.isMoveSync()) {
				Set<UnfoldedNode> sigma = mapActivity2sigma.get(move.getUnode());
				if (sigma != null) {
					mapSigma2subtrace.get(sigma).add(move);
				}
			}
		}

		for (Set<UnfoldedNode> sigma : partition) {
			mapSigma2sublog.get(sigma).add(mapSigma2subtrace.get(sigma), cardinality);
		}
	}

	public static void splitLoop(List<AlignedLog> result, IMTraceG<Move> trace,
			Collection<Set<UnfoldedNode>> partition, long cardinality,
			HashMap<Set<UnfoldedNode>, AlignedLog> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma) {
		IMTraceG<Move> partialTrace = new IMTraceG<Move>();

		Set<UnfoldedNode> lastSigma = partition.iterator().next();
		for (Move move : trace) {
			if (move.isMoveSync()) {
				if (!lastSigma.contains(move.getUnode())) {
					mapSigma2sublog.get(lastSigma).add(partialTrace, cardinality);
					partialTrace = new IMTraceG<Move>();
					lastSigma = mapActivity2sigma.get(move.getUnode());
				}
				partialTrace.add(move);
			}
		}
		mapSigma2sublog.get(lastSigma).add(partialTrace, cardinality);
	}
}
