package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.List;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeImpl;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Class to phase out UnfoldedNodes.
 * 
 * @author sleemans
 *
 */
public class IvMEfficientTree extends EfficientTreeImpl {
	private final List<UnfoldedNode> index2unfoldedNode;
	private final TObjectIntMap<UnfoldedNode> unfoldedNode2index;
	private final ProcessTree dTree;

	public IvMEfficientTree(ProcessTree tree) throws UnknownTreeNodeException {
		super(ProcessTree2EfficientTree.convert(tree).getTree(),
				ProcessTree2EfficientTree.convert(tree).getActivity2int(),
				ProcessTree2EfficientTree.convert(tree).getInt2activity());
		this.dTree = tree;

		index2unfoldedNode = TreeUtils.unfoldAllNodes(new UnfoldedNode(tree.getRoot()));

		unfoldedNode2index = new TObjectIntHashMap<>(10, 0.5f, -1);
		for (int i = 0; i < index2unfoldedNode.size(); i++) {
			unfoldedNode2index.put(index2unfoldedNode.get(i), i);
		}
	}

	public IvMEfficientTree(EfficientTree tree) {
		this(EfficientTree2processTree.convert(tree));
	}

	/**
	 * 
	 * @param unode
	 * @return The index of the node. If the node is not present (or is null),
	 *         will return -1.
	 */
	public int getIndex(UnfoldedNode unode) {
		return unfoldedNode2index.get(unode);
	}

	public UnfoldedNode getUnfoldedNode(int index) {
		return index2unfoldedNode.get(index);
	}

	public ProcessTree getDTree() {
		return dTree;
	}
}
