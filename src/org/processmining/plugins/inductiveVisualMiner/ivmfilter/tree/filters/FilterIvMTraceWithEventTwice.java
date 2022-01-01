package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;

public class FilterIvMTraceWithEventTwice implements IvMFilterBuilder<IvMTrace, IvMMove, IvMFilterGui> {

	@Override
	public String toString() {
		return "trace with completion event twice";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "trace with completion event twice";
	}

	@Override
	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui(toString(), decorator);
		result.add(result
				.createExplanation("Include traces that have two completion events that pass all the sub-filters."));
		return result;
	}

	@Override
	public IvMFilterTreeNode<IvMTrace> buildFilter(IvMFilterGui gui) {
		return new IvMFilterTreeNodeCompositeAbstract<IvMTrace, IvMMove>() {

			private static final long serialVersionUID = 8213030059677606305L;

			public boolean staysInLogA(IvMTrace x) {
				boolean first = false;
				for (IvMMove move : x) {
					if (targets(move)) {
						if (first) {
							return true;
						} else {
							first = true;
						}
					}
				}
				return false;
			}

			private boolean targets(IvMMove move) {
				if (move.getLifeCycleTransition() != PerformanceTransition.complete) {
					return false;
				}
				for (IvMFilterTreeNode<IvMMove> child : this) {
					if (!child.staysInLog(move)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public String getPrefix() {
				return "two completion events ";
			}

			public String getDivider() {
				return "and";
			}

			public boolean couldSomethingBeFiltered() {
				return true; //the empty trace and traces with a single event are always filtered
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