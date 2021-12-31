package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

public class FilterIvMMoveOr implements IvMFilterBuilder<IvMMove, IvMMove, IvMFilterGui> {

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
	public IvMFilterTreeNode<IvMMove> buildFilter(IvMFilterGui panel) {
		return new IvMFilterTreeNodeCompositeAbstract<IvMMove, IvMMove>() {

			private static final long serialVersionUID = -2705606899973613204L;

			public boolean staysInLogA(IvMMove x) {
				for (IvMFilterTreeNode<IvMMove> child : this) {
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
				for (IvMFilterTreeNode<IvMMove> child : this) {
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
