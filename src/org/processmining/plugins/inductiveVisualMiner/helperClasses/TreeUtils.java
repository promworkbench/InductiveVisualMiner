package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.ArrayList;
import java.util.List;

import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class TreeUtils {
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
}
