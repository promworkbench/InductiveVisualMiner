package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

public class FilterIvMMoveAnd implements IvMFilterBuilder<IvMMove, IvMMove, IvMFilterGui> {

	@Override
	public String toString() {
		return "and";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "and";
	}

	@Override
	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui(null, decorator);
		result.add(result.createExplanation("Include events that pass all of the sub-filters."));
		return result;
	}

	@Override
	public IvMFilterTreeNode<IvMMove> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeCompositeAbstract<IvMMove, IvMMove>() {

			private static final long serialVersionUID = -2705606899973613204L;

			public boolean staysInLogA(IvMMove x) {
				for (IvMFilterTreeNode<IvMMove> child : this) {
					if (!child.staysInLog(x)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public String getPrefix() {
				return "both";
			}

			public String getDivider() {
				return "and";
			}

			public boolean couldSomethingBeFiltered() {
				for (IvMFilterTreeNode<IvMMove> child : this) {
					if (child.couldSomethingBeFiltered()) {
						return true;
					}
				}
				return false;
			}
		};
	}

	@Override
	public boolean allowsChildren() {
		return true;
	}

	@Override
	public Class<IvMMove> getTargetClass() {
		return IvMMove.class;
	}

	@Override
	public Class<IvMMove> getChildrenTargetClass() {
		return IvMMove.class;
	}

	@Override
	public void setAttributesInfo(IvMAttributesInfo attributesInfo, IvMFilterGui gui) {

	}

}
