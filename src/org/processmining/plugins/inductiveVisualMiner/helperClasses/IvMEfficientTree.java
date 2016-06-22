package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

/**
 * Class to phase out UnfoldedNodes.
 * 
 * @author sleemans
 *
 */
public class IvMEfficientTree extends EfficientTree {
	private final List<UnfoldedNode> index2unfoldedNode;
	private final TObjectIntMap<UnfoldedNode> unfoldedNode2index;
	private final ProcessTree dTree;

	public IvMEfficientTree(ProcessTree tree) throws UnknownTreeNodeException {
		super(tree);
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

	public int getRoot() {
		return 0;
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

	/**
	 * 
	 * @param parent
	 * @param grandChild
	 * @return The child of parent that contains grandChild. If grandChild is
	 *         not a child of parent, will return -1.
	 */
	public int getChildWith(int parent, int grandChild) {
		for (int child : getChildren(parent)) {
			if (isParentOf(child, grandChild)) {
				return child;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param parent
	 * @param grandChild
	 * @return The number of the child within parent that contains grandChild. If grandChild is
	 *         not a child of parent, will return -1.
	 */
	public int getChildNumberWith(int parent, int grandChild) {
		int childNumber = 0;
		for (int child : getChildren(parent)) {
			if (isParentOf(child, grandChild)) {
				return childNumber;
			}
			childNumber++;
		}
		return -1;
	}

	public int getLowestCommonParent(int nodeA, int nodeB) {
		if (nodeA > nodeB) {
			return getLowestCommonParent(nodeB, nodeA);
		}
		if (nodeA == nodeB) {
			return nodeA;
		}

		int lowestCommonParent = nodeA;
		while (!isParentOf(lowestCommonParent, nodeB)) {
			lowestCommonParent = getParent(lowestCommonParent);
		}
		return lowestCommonParent;
	}

	/**
	 * 
	 * @param parent
	 * @param child
	 * @return Whether the child is a direct or indirect child of parent.
	 */
	public boolean isParentOf(int parent, int child) {
		if (parent > child) {
			return false;
		}
		return traverse(parent) > child;
	}

	public ProcessTree getDTree() {
		return dTree;
	}

	public Iterable<Integer> getAllNodes() {
		return getAllNodes(getRoot());
	}

	public Iterable<Integer> getAllNodes(final int child) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = child - 1;

					public int findNext() {
						int next = now + 1;
						while (next < getTree().length && getTree()[next] == skip) {
							next++;
						}
						if (next == getTree().length) {
							return -1;
						}
						return next;
					}

					public Integer next() {
						now = findNext();
						return now;
					}

					public boolean hasNext() {
						return findNext() != -1;
					}
					
					public void remove() {
						
					}
				};
			}
		};
	}
}
