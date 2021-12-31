package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class FilterIvMTraceAny implements IvMFilterBuilder<IvMTrace, Object, IvMFilterGui> {

	public String toString() {
		return "any trace";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "any trace";
	}

	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		return new IvMFilterGui(null, decorator);
	}

	public IvMFilterTreeNode<IvMTrace> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeLeafAbstract<IvMTrace>() {

			public boolean staysInLogA(IvMTrace element) {
				return true;
			}

			public void getExplanation(StringBuilder result, int indent) {
				result.append(StringUtils.repeat("\t", indent));
				result.append("any trace");
			}

			public boolean couldSomethingBeFiltered() {
				return false;
			}
		};
	}

	public boolean allowsChildren() {
		return false;
	}

	public Class<IvMTrace> getTargetClass() {
		return IvMTrace.class;
	}

	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, IvMFilterGui gui) {

	}
}