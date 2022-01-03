package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterXEventOr extends IvMFilterBuilderAbstract<XEvent, XEvent, IvMFilterGui> {

	@Override
	public String toString() {
		return "or";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "or";
	}

	@Override
	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui(null, decorator);
		result.add(result.createExplanation("Include events that pass any of the sub-filters."));
		return result;
	}

	@Override
	public IvMFilterTreeNode<XEvent> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeCompositeAbstract<XEvent, XEvent>() {

			private static final long serialVersionUID = -2705606899973613204L;

			public boolean staysInLogA(XEvent x) {
				for (IvMFilterTreeNode<XEvent> child : this) {
					if (child.staysInLog(x)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getPrefix() {
				return "any of";
			}

			public String getDivider() {
				return "or";
			}

			public boolean couldSomethingBeFiltered() {
				for (IvMFilterTreeNode<XEvent> child : this) {
					if (!child.couldSomethingBeFiltered()) {
						return false;
					}
				}
				return true;
			}
		};
	}

	@Override
	public boolean allowsChildren() {
		return true;
	}

	@Override
	public Class<XEvent> getTargetClass() {
		return XEvent.class;
	}

	@Override
	public Class<XEvent> getChildrenTargetClass() {
		return XEvent.class;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo, IvMFilterGui gui) {

	}

}
