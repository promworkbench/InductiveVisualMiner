package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeaf;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class FilterIvMTraceNoop implements IvMFilterBuilder<IvMTrace, Object, IvMFilterGui> {

	public String toString() {
		return "all traces";
	}

	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui("no filter", decorator) {
			private static final long serialVersionUID = 110211772022409817L;

			protected void setForegroundRecursively(Color colour) {

			}
		};
		return result;
	}

	public IvMFilterTreeNode<IvMTrace> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeLeaf<IvMTrace>() {

			public boolean staysInLog(IvMTrace element) {
				return true;
			}

			public void getExplanation(StringBuilder result, int indent) {
				result.append(StringUtils.repeat("\t", indent));
				result.append("no filter");
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