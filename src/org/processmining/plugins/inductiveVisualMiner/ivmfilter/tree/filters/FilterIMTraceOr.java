package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterIMTraceOr extends IvMFilterBuilderAbstract<IMTrace, IMTrace, IvMFilterGui> {

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
		result.add(result.createExplanation("Include traces that pass any of the sub-filters."));
		return result;
	}

	@Override
	public IvMFilterTreeNode<IMTrace> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeCompositeAbstract<IMTrace, IMTrace>() {

			private static final long serialVersionUID = -2705606899973613204L;

			public boolean staysInLogA(IMTrace x) {
				for (IvMFilterTreeNode<IMTrace> child : this) {
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
				for (IvMFilterTreeNode<IMTrace> child : this) {
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
	public Class<IMTrace> getTargetClass() {
		return IMTrace.class;
	}

	@Override
	public Class<IMTrace> getChildrenTargetClass() {
		return IMTrace.class;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo, IvMFilterGui gui) {

	}

}
