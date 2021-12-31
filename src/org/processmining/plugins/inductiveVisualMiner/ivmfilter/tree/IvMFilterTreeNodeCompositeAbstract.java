package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

public abstract class IvMFilterTreeNodeCompositeAbstract<X, Y> extends ArrayList<IvMFilterTreeNode<Y>>
		implements IvMFilterTreeNodeComposite<X, Y> {

	private static final long serialVersionUID = 2635220824247965072L;

	public abstract String getDivider();

	public abstract String getPrefix();

	public String toString() {
		return this.getClass().toString();
	}

	@Override
	public void getExplanation(StringBuilder result, int indent) {
		result.append(StringUtils.repeat("\t", indent));
		result.append("having ");
		result.append(getPrefix());
		result.append("\n");
		for (Iterator<IvMFilterTreeNode<Y>> it = iterator(); it.hasNext();) {
			IvMFilterTreeNode<Y> child = it.next();
			child.getExplanation(result, indent + 1);

			if (it.hasNext()) {
				result.append("\n");
				result.append(StringUtils.repeat("\t", indent));
				result.append(getDivider());
				result.append("\n");
			}
		}
	}

	protected abstract boolean staysInLogA(X element);

	@Override
	public boolean staysInLog(X element) {
		if (!couldSomethingBeFiltered()) {
			return true;
		}
		return staysInLogA(element);
	}
}