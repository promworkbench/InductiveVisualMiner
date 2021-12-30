package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeaf;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

public class FilterIvMMoveNoop implements IvMFilterBuilder<IvMMove, Object, IvMFilterGui> {

	public String toString() {
		return "all events";
	}

	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui("no filter", decorator) {
			private static final long serialVersionUID = 110211772022409817L;

			protected void setForegroundRecursively(Color colour) {

			}
		};
		return result;
	}

	public IvMFilterTreeNode<IvMMove> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeLeaf<IvMMove>() {

			public boolean staysInLog(IvMMove element) {
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

	public Class<IvMMove> getTargetClass() {
		return IvMMove.class;
	}

	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, IvMFilterGui gui) {

	}
}