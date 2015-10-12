package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class LogSplitter {
	
	/**
	 * Holds information for log splitting
	 * @author sleemans
	 *
	 * @param <X>
	 */
	public static class SigmaMaps <X> {
		public List<Set<UnfoldedNode>> partition = new LinkedList<Set<UnfoldedNode>>();
		public List<List<X>> sublogs = new ArrayList<List<X>>();
		public HashMap<Set<UnfoldedNode>, List<X>> mapSigma2subtrace = new HashMap<Set<UnfoldedNode>, List<X>>();
		public HashMap<UnfoldedNode, Set<UnfoldedNode>> mapUnode2sigma= new HashMap<UnfoldedNode, Set<UnfoldedNode>>();
	}

	/**
	 * Provides the information to split a log
	 * @param unode
	 * @return
	 */
	public static <X> SigmaMaps<X> makeSigmaMaps(
			UnfoldedNode unode) {
		SigmaMaps<X> r = new SigmaMaps<X>();
		
		//make a partition
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uchild = unode.unfoldChild(child);
			r.partition.add(new HashSet<UnfoldedNode>(TreeUtils.unfoldAllNodes(uchild)));
		}

		//map activities to sigmas
		for (Set<UnfoldedNode> sigma : r.partition) {
			List<X> subtrace = new ArrayList<X>();
			r.sublogs.add(subtrace);
			r.mapSigma2subtrace.put(sigma, subtrace);
			for (UnfoldedNode unode2 : sigma) {
				r.mapUnode2sigma.put(unode2, sigma);
			}
		}
		
		return r;
	}
}
