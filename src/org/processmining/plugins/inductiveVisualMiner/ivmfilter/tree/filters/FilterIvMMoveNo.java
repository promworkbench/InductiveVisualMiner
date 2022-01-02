package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

public class FilterIvMMoveNo extends IvMFilterBuilderAbstract<IvMMove, Object, IvMFilterGui> {

	public String toString() {
		return "no event";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "no event";
	}

	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		return new IvMFilterGui(null, decorator);
	}

	public IvMFilterTreeNode<IvMMove> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeLeafAbstract<IvMMove>() {

			public boolean staysInLogA(IvMMove element) {
				return true;
			}

			public void getExplanation(StringBuilder result, int indent) {
				result.append(StringUtils.repeat("\t", indent));
				result.append("no event");
			}

			public boolean couldSomethingBeFiltered() {
				return true;
			}
		};
	}

	public boolean allowsChildren() {
		return false;
	}

	public Class<IvMMove> getTargetClass() {
		return IvMMove.class;
	}

	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, IvMFilterGui gui) {

	}
}