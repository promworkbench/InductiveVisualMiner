package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view;

public interface FilterHandler {
	public void filterChanged(IvMFilterTreeNodeView<?> nodeView);

	public <X> void addChild(IvMFilterTreeNodeView<X> parent, boolean selectNewChild);
}