package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * A choice is a list of nodes (from which to choose one) or a single node (to
 * choose whether to execute that node or not).
 * 
 * @author sander
 *
 */
public class Choice {
	TIntSet nodes = new TIntHashSet(10, 0.5f, -1);
	TIntSet ids = new TIntHashSet(10, 0.5f, -1);

	@Override
	public String toString() {
		return getId();
		//return nodes.toString() + "i" + ids.toString();
	}

	/**
	 * Human-readable string.
	 * 
	 * @param model
	 * @return
	 */
	public String toString(IvMModel model) {
		StringBuilder s = new StringBuilder();

		for (TIntIterator it = nodes.iterator(); it.hasNext();) {
			int node = it.next();

			if (model.isActivity(node)) {
				s.append(model.getActivityName(node));
			} else if (model.isTau(node)) {
				s.append("[skip]");
			} else if (model.isTree()) {
				s.append("[" + model.getTree().getNodeType(node) + "]");
			} else {
				s.append("[" + node + "]");
			}

			if (it.hasNext()) {
				s.append(", ");
			}
		}
		return s.toString();
	}

	public String getId() {
		StringBuilder result = new StringBuilder();
		for (TIntIterator it = nodes.iterator(); it.hasNext();) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append("-");
			}
		}
		result.append("i");
		for (TIntIterator it = ids.iterator(); it.hasNext();) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append("-");
			}
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ids == null) ? 0 : ids.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Choice other = (Choice) obj;
		if (ids == null) {
			if (other.ids != null) {
				return false;
			}
		} else if (!ids.equals(other.ids)) {
			return false;
		}
		if (nodes == null) {
			if (other.nodes != null) {
				return false;
			}
		} else if (!nodes.equals(other.nodes)) {
			return false;
		}
		return true;
	}
}