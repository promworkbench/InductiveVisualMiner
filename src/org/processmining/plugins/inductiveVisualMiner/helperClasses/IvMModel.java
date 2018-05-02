package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;

public class IvMModel {

	private final IvMEfficientTree tree;
	private final Dfg dfg;

	public IvMModel(EfficientTree tree) {
		this.tree = new IvMEfficientTree(tree);
		this.dfg = null;
	}

	public IvMModel(Dfg dfg) {
		this.tree = null;
		this.dfg = dfg;
	}

	public boolean isTree() {
		return tree != null;
	}

	public IvMEfficientTree getTree() {
		return tree;
	}

	public boolean isDfg() {
		return dfg != null;
	}

	public Dfg getDfg() {
		return dfg;
	}

	public String getActivityName(int node) {
		if (isTree()) {
			return tree.getActivityName(node);
		} else {
			return dfg.getActivityOfIndex(node).getId();
		}
	}

	public boolean isActivity(int node) {
		if (isTree()) {
			return tree.isActivity(node);
		} else {
			return node >= 0;
		}
	}

	public Iterable<Integer> getAllNodes() {
		if (isTree()) {
			return EfficientTreeUtils.getAllNodes(tree);
		} else {
			return new Iterable<Integer>() {
				public Iterator<Integer> iterator() {
					return new Iterator<Integer>() {
						int now = -1;

						public Integer next() {
							now++;
							return now;
						}

						public boolean hasNext() {
							return now < dfg.getNumberOfActivities() - 1;
						}
					};
				}
			};
		}
	}

	public boolean isTau(int node) {
		if (isTree()) {
			return tree.isTau(node);
		} else {
			return false;
		}
	}

	public boolean isParentOf(int parent, int child) {
		if (isTree()) {
			return EfficientTreeUtils.isParentOf(tree, parent, child);
		} else {
			return false;
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dfg == null) ? 0 : dfg.hashCode());
		result = prime * result + ((tree == null) ? 0 : tree.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IvMModel other = (IvMModel) obj;
		if (dfg == null) {
			if (other.dfg != null)
				return false;
		} else if (!dfg.equals(other.dfg))
			return false;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.equals(other.tree))
			return false;
		return true;
	}
}
