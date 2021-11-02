package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * A choice is a list of nodes (from which to choose one) or a single node (to
 * choose whether to execute that node or not).
 * 
 * @author sander
 *
 */
public class Choice {
	TIntList nodes = new TIntArrayList();
	TIntList ids = new TIntArrayList();

	@Override
	public String toString() {
		return getId();
		//return nodes.toString() + "i" + ids.toString();
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