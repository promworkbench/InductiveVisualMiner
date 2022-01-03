package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterIMTraceAny extends IvMFilterBuilderAbstract<IMTrace, Object, IvMFilterGui> {

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

	public IvMFilterTreeNode<IMTrace> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeLeafAbstract<IMTrace>() {

			public boolean staysInLogA(IMTrace element) {
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

	public Class<IMTrace> getTargetClass() {
		return IMTrace.class;
	}

	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public void setAttributesInfo(AttributesInfo attributesInfo, IvMFilterGui gui) {

	}

}
