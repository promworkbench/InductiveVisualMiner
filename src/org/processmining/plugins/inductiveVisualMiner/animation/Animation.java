package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

import com.kitfox.svg.Path;

public class Animation {
	public static String getPath(DotPanel panel, Path path) {
		return panel.getAttributeOf(path, "d");
	}

	public static String positionTrace(IMTraceG<Move> trace, UnfoldedNode unode) {

		System.out.println(" enter " + unode + " with trace " + trace);
		if (unode.getNode() instanceof Manual) {
			System.out.println(" execute activity " + unode);
		} else if (unode.getNode() instanceof Automatic) {
			System.out.println(" execute tau " + unode);
		} else if (unode.getBlock() instanceof DefLoop || unode.getBlock() instanceof XorLoop) {
			List<List<IMTraceG<Move>>> sublogs = getSublogs(unode, trace);
		} else {
			List<List<IMTraceG<Move>>> sublogs = getSublogs(unode, trace);
			Iterator<List<IMTraceG<Move>>> itSublog = sublogs.iterator();
			Iterator<Node> itChild = unode.getBlock().getChildren().iterator();
			while (itSublog.hasNext() && itChild.hasNext()) {
				IMTraceG<Move> childTrace = itSublog.next().get(0);
				UnfoldedNode child = unode.unfoldChild(itChild.next());
				positionTrace(childTrace, child);
			}
		}

		System.out.println(" exit " + unode);
		return "";
	}
	
	private static List<List<IMTraceG<Move>>> getSublogs(UnfoldedNode unode, IMTraceG<Move> trace) {

		//make a partition
		List<Set<UnfoldedNode>> partition = new LinkedList<Set<UnfoldedNode>>();
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uchild = unode.unfoldChild(child);
			partition.add(new HashSet<UnfoldedNode>(AlignedLogMetrics.unfoldAllNodes(uchild)));
		}

		//split the log
		return split(trace, partition, unode.getBlock() instanceof XorLoop);
	}

	public static List<List<IMTraceG<Move>>> split(IMTraceG<Move> trace, List<Set<UnfoldedNode>> partition, boolean loop) {
		List<List<IMTraceG<Move>>> result = new ArrayList<List<IMTraceG<Move>>>();

		//map activities to sigmas
		HashMap<Set<UnfoldedNode>, List<IMTraceG<Move>>> mapSigma2sublog = new HashMap<Set<UnfoldedNode>, List<IMTraceG<Move>>>();
		HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma = new HashMap<UnfoldedNode, Set<UnfoldedNode>>();
		for (Set<UnfoldedNode> sigma : partition) {
			List<IMTraceG<Move>> sublog = new ArrayList<IMTraceG<Move>>();
			result.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (UnfoldedNode activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}

		if (!loop) {
			splitParallel(result, trace, partition, mapSigma2sublog, mapActivity2sigma);
		} else {
			splitLoop(result, trace, partition, mapSigma2sublog, mapActivity2sigma);
		}

		return result;
	}
	
	public static void splitParallel(List<List<IMTraceG<Move>>> result, IMTraceG<Move> trace, List<Set<UnfoldedNode>> partition,
			HashMap<Set<UnfoldedNode>, List<IMTraceG<Move>>> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma) {

		//add a new trace to every sublog
		HashMap<Set<UnfoldedNode>, IMTraceG<Move>> mapSigma2subtrace = new HashMap<Set<UnfoldedNode>, IMTraceG<Move>>();
		for (Set<UnfoldedNode> sigma : partition) {
			IMTraceG<Move> subtrace = new IMTraceG<Move>();
			mapSigma2subtrace.put(sigma, subtrace);
		}

		for (Move move : trace) {
			if (move.unode != null) {
				Set<UnfoldedNode> sigma = mapActivity2sigma.get(move.unode);
				if (sigma != null) {
					mapSigma2subtrace.get(sigma).add(move);
				}
			}
		}

		for (Set<UnfoldedNode> sigma : partition) {
			mapSigma2sublog.get(sigma).add(mapSigma2subtrace.get(sigma));
		}
	}
	
	public static void splitLoop(List<List<IMTraceG<Move>>> result, IMTraceG<Move> trace,
			Collection<Set<UnfoldedNode>> partition,
			HashMap<Set<UnfoldedNode>, List<IMTraceG<Move>>> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma) {
		IMTraceG<Move> partialTrace = new IMTraceG<Move>();

		Set<UnfoldedNode> lastSigma = partition.iterator().next();
		for (Move move : trace) {
			if (move.unode != null) {
				if (!lastSigma.contains(move.unode)) {
					mapSigma2sublog.get(lastSigma).add(partialTrace);
					partialTrace = new IMTraceG<Move>();
					lastSigma = mapActivity2sigma.get(move.unode);
				}
				partialTrace.add(move);
			}
		}
		mapSigma2sublog.get(lastSigma).add(partialTrace);
	}
}
