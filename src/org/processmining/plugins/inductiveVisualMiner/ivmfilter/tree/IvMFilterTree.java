package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

import java.util.Iterator;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

public class IvMFilterTree<X> {

	private final String prefix;
	private final IvMFilterTreeNode<X> root;

	public IvMFilterTree(IvMFilterTreeNode<X> root, String prefix) {
		this.root = root;
		this.prefix = prefix;
	}

	public IvMFilterTreeNode<X> getRoot() {
		return root;
	}

	public String getExplanation() {
		if (root.couldSomethingBeFiltered()) {
			StringBuilder result = new StringBuilder();
			result.append(prefix);
			result.append(" traces ");
			root.getExplanation(result, 0);
			result.append(".");
			return result.toString();
		} else {
			return prefix + " all traces.";
		}
	}

	public boolean couldSomethingBeFiltered() {
		return root.couldSomethingBeFiltered();
	}

	public boolean staysInLog(X element) {
		return root.staysInLog(element);
	}

	public void filter(Iterator<X> it, IvMCanceller canceller) {
		if (!couldSomethingBeFiltered()) {
			return;
		}

		while (it.hasNext()) {
			X element = it.next();

			if (!staysInLog(element)) {
				it.remove();
			}

			if (canceller.isCancelled()) {
				return;
			}
		}
	}
}