package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterXEventAny extends IvMFilterBuilderAbstract<XEvent, Object, IvMFilterGui> {

	public String toString() {
		return "any event";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "any event";
	}

	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		return new IvMFilterGui(null, decorator);
	}

	public IvMFilterTreeNode<XEvent> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeLeafAbstract<XEvent>() {

			public boolean staysInLogA(XEvent element) {
				return true;
			}

			public void getExplanation(StringBuilder result, int indent) {
				result.append(StringUtils.repeat("\t", indent));
				result.append("any event");
			}

			public boolean couldSomethingBeFiltered() {
				return false;
			}
		};
	}

	public boolean allowsChildren() {
		return false;
	}

	public Class<XEvent> getTargetClass() {
		return XEvent.class;
	}

	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public void setAttributesInfo(AttributesInfo attributesInfo, IvMFilterGui gui) {

	}

}
