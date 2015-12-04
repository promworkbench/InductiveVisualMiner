package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class TreeUtils {

	public static List<UnfoldedNode> unfoldAllActivities(ProcessTree tree) {
		return unfoldAllActivities(new UnfoldedNode(tree.getRoot()));
	}

	public static List<UnfoldedNode> unfoldAllActivities(UnfoldedNode unode) {
		List<UnfoldedNode> result = new ArrayList<UnfoldedNode>();
		if (unode.getNode() instanceof Manual) {
			result.add(unode);
		}
		if (unode.getNode() instanceof Block) {
			for (Node child : unode.getBlock().getChildren()) {
				result.addAll(unfoldAllNodes(unode.unfoldChild(child)));
			}
		}
		return result;
	}

	/**
	 * 
	 * @param tree
	 * @return all nodes in the tree
	 */
	public static List<UnfoldedNode> unfoldAllNodes(ProcessTree tree) {
		return unfoldAllNodes(new UnfoldedNode(tree.getRoot()));
	}

	/**
	 * 
	 * @param unode
	 * @return all nodes in the tree.
	 */
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

	/**
	 * 
	 * @param unode1
	 * @param unode2
	 * @return whether both are in parallel
	 */
	public static boolean areParallel(UnfoldedNode unode1, UnfoldedNode unode2) {
		return getLowestCommonParent(unode1, unode2) instanceof And;
	}

	/**
	 * 
	 * @param unode1
	 * @param unode2
	 * @return the lowest common parent
	 */
	public static Node getLowestCommonParent(UnfoldedNode unode1, UnfoldedNode unode2) {
		Iterator<Node> it1 = unode1.getPath().iterator();
		Iterator<Node> it2 = unode2.getPath().iterator();
		Node n1 = it1.next();
		Node n2 = it2.next();
		Node lastEqual = null;
		while (it1.hasNext() && it2.hasNext()) {
			if (!n1.equals(n2)) {
				return lastEqual;
			}
			lastEqual = n1;
			n1 = it1.next();
			n2 = it2.next();
		}
		return lastEqual;
	}

	/**
	 * 
	 * @param tree
	 * @return a set of the names of all leaves in this tree.
	 */
	public static Set<String> getNodeNames(ProcessTree tree) {
		Set<String> result = new THashSet<>();
		for (UnfoldedNode unode : TreeUtils.unfoldAllNodes(new UnfoldedNode(tree.getRoot()))) {
			String name = unode.getNode().getName();
			if (unode.getNode() instanceof Manual) {
				result.add(name);
			}
		}
		return result;
	}
}
