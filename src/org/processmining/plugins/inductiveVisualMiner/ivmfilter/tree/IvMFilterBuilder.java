package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public interface IvMFilterBuilder<X, Y, G extends IvMFilterGui> extends Comparable<IvMFilterBuilder<?, ?, ?>> {

	/**
	 * toString must be overridden with the name of the filter.
	 * 
	 * @return
	 */
	public String toString();

	/**
	 * 
	 * @param panel
	 * @return a short description of the currently set-up filter (might be used
	 *         in the tree view)
	 */
	public String toString(G panel);

	public boolean allowsChildren();

	public Class<X> getTargetClass();

	public Class<Y> getChildrenTargetClass();

	public G createGui(Runnable onUpdate, IvMDecoratorI decorator);

	/**
	 * Creates a filter node without children. Note that this method may use the
	 * filter's gui to obtain settings, but must copy these in order to
	 * guarantee thread-safeness. The returned treeNode must not contain
	 * pointers to gui elements.
	 * 
	 * @return
	 */
	public IvMFilterTreeNode<X> buildFilter(G gui);

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, G gui);

	/**
	 * Add a communication channel to/from a filter. The implementation may use
	 * the gui, as this method will only be called on the gui thread.
	 * 
	 * @param <TI>
	 * @param <TO>
	 * @param <FI>
	 * @param <FO>
	 * @param channel
	 * @param gui
	 */
	public <TI, TO, FI, FO> void setCommunicationChannel(FilterCommunicator<TI, TO, FI, FO> channel, G gui);
}