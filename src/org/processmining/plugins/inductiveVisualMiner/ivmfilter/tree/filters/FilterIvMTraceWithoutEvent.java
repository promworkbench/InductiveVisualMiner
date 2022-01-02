package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class FilterIvMTraceWithoutEvent extends IvMFilterBuilderAbstract<IvMTrace, IvMMove, IvMFilterGui> {

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
	public IvMFilterTreeNode<IvMTrace> buildFilter(IvMFilterGui gui) {
		return new IvMFilterTreeNodeCompositeAbstract<IvMTrace, IvMMove>() {

			private static final long serialVersionUID = 8213030059677606305L;

			public boolean staysInLogA(IvMTrace x) {
				for (IvMMove move : x) {
					if (targets(move)) {
						return false;
					}
				}
				return true;
			}

			private boolean targets(IvMMove move) {
				for (IvMFilterTreeNode<IvMMove> child : this) {
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
	public Class<IvMTrace> getTargetClass() {
		return IvMTrace.class;
	}

	@Override
	public Class<IvMMove> getChildrenTargetClass() {
		return IvMMove.class;
	}

	@Override
	public void setAttributesInfo(IvMAttributesInfo attributesInfo, IvMFilterGui gui) {

	}
}