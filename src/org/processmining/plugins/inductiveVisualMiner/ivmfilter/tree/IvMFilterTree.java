package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

public class IvMFilterTree<X> {

	private IvMFilterTreeNode<X> root;

	public IvMFilterTree(IvMFilterTreeNode<X> root) {
		this.root = root;
	}

	public IvMFilterTreeNode<X> getRoot() {
		return root;
	}

	public void setRoot(IvMFilterTreeNode<X> root) {
		this.root = root;
	}

	public String getExplanation() {
		if (root.couldSomethingBeFiltered()) {
			StringBuilder result = new StringBuilder();
			result.append("Include traces ");
			root.getExplanation(result, 0);
			result.append(".");
			return result.toString();
		} else {
			return "Include all traces.";
		}
	}
}