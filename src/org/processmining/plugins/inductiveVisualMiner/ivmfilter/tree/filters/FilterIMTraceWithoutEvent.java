package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterIMTraceWithoutEvent extends IvMFilterBuilderAbstract<IMTrace, XEvent, IvMFilterGui> {

	@Override
	public String toString() {
		return "without event";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "without event";
	}

	@Override
	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui(toString(), decorator);
		result.add(result.createExplanation("Include traces that have no event that passes all the sub-filters."));
		return result;
	}

	@Override
	public IvMFilterTreeNode<IMTrace> buildFilter(IvMFilterGui gui) {
		return new IvMFilterTreeNodeCompositeAbstract<IMTrace, XEvent>() {

			private static final long serialVersionUID = 8213030059677606305L;

			public boolean staysInLogA(IMTrace x) {
				for (XEvent move : x) {
					if (targets(move)) {
						return false;
					}
				}
				return true;
			}

			private boolean targets(XEvent move) {
				for (IvMFilterTreeNode<XEvent> child : this) {
					if (!child.staysInLog(move)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public String getPrefix() {
				return "no event ";
			}

			public String getDivider() {
				return "and";
			}

			public boolean couldSomethingBeFiltered() {
				return true; //empty traces always pass this filter
			}
		};
	}

	@Override
	public boolean allowsChildren() {
		return true;
	}

	@Override
	public Class<IMTrace> getTargetClass() {
		return IMTrace.class;
	}

	@Override
	public Class<XEvent> getChildrenTargetClass() {
		return XEvent.class;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo, IvMFilterGui gui) {

	}
}