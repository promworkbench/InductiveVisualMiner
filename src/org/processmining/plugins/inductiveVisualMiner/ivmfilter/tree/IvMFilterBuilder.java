package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public interface IvMFilterBuilder<X, Y, G extends IvMFilterGui> {

	/**
	 * toString must be overridden with the name of the filter.
	 * 
	 * @return
	 */
	public String toString();

	public boolean allowsChildren();

	public Class<X> getTargetClass();

	public Class<Y> getChildrenTargetClass();

	public G createGui(Runnable onUpdate, IvMDecoratorI decorator);

	/**
	 * Creates a filter node without children.
	 * 
	 * @return
	 */
	public IvMFilterTreeNode<X> buildFilter(G gui);

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, G gui);
}