package org.processmining.plugins.inductiveVisualMiner.causal;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
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
	public TIntSet nodes = new TIntHashSet(10, 0.5f, -1);
	public TIntList ids = new TIntArrayList();

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

			s.append(node2string(model, node));

			if (it.hasNext()) {
				s.append(", ");
			}
		}
		if (!ids.isEmpty()) {
			s.append(" i");
			for (TIntIterator it = ids.iterator(); it.hasNext();) {
				int id = it.next();
				s.append(id);
				if (it.hasNext()) {
					s.append("-");
				}
			}
		}
		return s.toString();
	}

	public static String node2string(IvMModel model, int node) {
		if (model.isActivity(node)) {
			return model.getActivityName(node);
		} else if (model.isTau(node)) {
			return "[skip " + node + "]";
		} else if (model.isTree()) {
			return "[" + model.getTree().getNodeType(node) + " " + node + "]";
		} else {
			return "[" + node + "]";
		}
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