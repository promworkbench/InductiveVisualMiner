package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.Iterator;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;

public class IvMModel {

	private final IvMEfficientTree tree;
	private final DirectlyFollowsModel dfg;
	private final AcceptingPetriNet net;
	private final Transition[] index2transition;
	private Source source;

	public static enum Source {
		mined, user
	}

	public IvMModel(EfficientTree tree) {
		this.tree = new IvMEfficientTree(tree);
		this.dfg = null;
		this.net = null;
		index2transition = null;
	}

	public IvMModel(DirectlyFollowsModel dfg) {
		this.tree = null;
		this.dfg = dfg;
		this.net = null;
		index2transition = null;
	}

	/**
	 * The given net does not need to be a sound workflow net, but from every
	 * reachable marking, it must be possible to reach a final marking.
	 * 
	 * @param net
	 */
	public IvMModel(AcceptingPetriNet net) {
		this.tree = null;
		this.dfg = null;
		this.net = net;

		index2transition = new Transition[net.getNet().getTransitions().size()];
		int i = 0;
		for (Transition transition : net.getNet().getTransitions()) {
			index2transition[i] = transition;
			i++;
		}
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

	public DirectlyFollowsModel getDfg() {
		return dfg;
	}

	public boolean isNet() {
		return net != null;
	}

	public AcceptingPetriNet getNet() {
		return net;
	}

	public Transition getNetTransition(int node) {
		return index2transition[node];
	}

	public String getActivityName(int node) {
		if (isTree()) {
			return tree.getActivityName(node);
		} else if (isDfg()) {
			return dfg.getNodeOfIndex(node);
		} else if (isNet()) {
			return index2transition[node].getLabel();
		}
		throw new RuntimeException("model not supported");
	}

	public boolean isActivity(int node) {
		if (isTree()) {
			return tree.isActivity(node);
		} else if (isDfg()) {
			return node >= 0;
		} else if (isNet()) {
			return true;
		}
		throw new RuntimeException("model not supported");
	}

	public Iterable<Integer> getAllNodes() {
		if (isTree()) {
			return EfficientTreeUtils.getAllNodes(tree);
		} else if (isDfg()) {
			return dfg.getNodeIndices();
		} else if (isNet()) {
			return new Iterable<Integer>() {
				public Iterator<Integer> iterator() {
					return new Iterator<Integer>() {
						int now = -1;

						public Integer next() {
							now++;
							return now;
						}

						public boolean hasNext() {
							return now < net.getNet().getTransitions().size() - 1;
						}
					};
				}
			};
		}
		throw new RuntimeException("model not supported");
	}

	public boolean isTau(int node) {
		if (isTree()) {
			return tree.isTau(node);
		} else if (isDfg()) {
			return node < 0;
		} else if (isNet()) {
			return index2transition[node].isInvisible();
		}
		throw new RuntimeException("model not supported");
	}

	public boolean isParentOf(int parent, int child) {
		if (isTree()) {
			return EfficientTreeUtils.isParentOf(tree, parent, child);
		} else if (isDfg()) {
			return false;
		} else if (isNet()) {
			return false;
		}
		throw new RuntimeException("model not supported");
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dfg == null) ? 0 : dfg.hashCode());
		result = prime * result + ((tree == null) ? 0 : tree.hashCode());
		result = prime * result + ((net == null) ? 0 : net.hashCode());
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
		if (net == null) {
			if (other.net != null)
				return false;
		} else if (!net.equals(other.net))
			return false;
		return true;
	}

	public int getMaxNumberOfNodes() {
		if (isTree()) {
			return tree.getMaxNumberOfNodes();
		} else if (isDfg()) {
			return dfg.getAllNodeNames().length;
		} else if (isNet()) {
			return net.getNet().getTransitions().size();
		}
		return this.tree.getMaxNumberOfNodes();
	}

	/**
	 * Get the one who constructed this model. Default is "mined"
	 * 
	 * @return
	 */
	public Source getSource() {
		if (source == null) {
			return Source.mined;
		}
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
}
