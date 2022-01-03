package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class FilterIvMTraceEndsWithEvent extends IvMFilterBuilderAbstract<IvMTrace, IvMMove, IvMFilterGui> {

	@Override
	public String toString() {
		return "ends with event";
	}

	@Override
	public String toString(IvMFilterGui panel) {
		return "ends with event";
	}

	@Override
	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui(toString(), decorator);
		result.add(result.createExplanation("Include traces of which the last event passes all the sub-filters."));
		return result;
	}

	@Override
	public IvMFilterTreeNode<IvMTrace> buildFilter(IvMFilterGui gui) {
		return new IvMFilterTreeNodeCompositeAbstract<IvMTrace, IvMMove>() {

			private static final long serialVersionUID = 8213030059677606305L;

			public boolean staysInLogA(IvMTrace x) {
				if (x.isEmpty()) {
					return false;
				}

				//Try whether the last move can be matched
				if (targets(x.get(x.size() - 1))) {
					return true;
				}

				//Find the last log event move and match it
				for (int i = x.size() - 1; i >= 0; i--) {
					IvMMove move = x.get(i);
					if (move.hasAttributes()) {
						return targets(move);
					}
				}
				return false;
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
				return "as last event ";
			}

			public String getDivider() {
				return "and";
			}

			public boolean couldSomethingBeFiltered() {
				return true; //empty traces are always filtered
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