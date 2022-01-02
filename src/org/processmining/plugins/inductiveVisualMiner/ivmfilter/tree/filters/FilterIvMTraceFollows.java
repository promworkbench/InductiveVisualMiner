package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeComposite;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FilterIvMTraceFollows extends IvMFilterBuilderAbstract<IvMTrace, IvMMove, FilterIvMTraceFollowsPanel> {

	public String toString() {
		return "follows";
	}

	public String toString(FilterIvMTraceFollowsPanel panel) {
		return "follows";
	}

	public boolean allowsChildren() {
		return true;
	}

	public Class<IvMTrace> getTargetClass() {
		return IvMTrace.class;
	}

	public Class<IvMMove> getChildrenTargetClass() {
		return IvMMove.class;
	}

	public FilterIvMTraceFollowsPanel createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		return new FilterIvMTraceFollowsPanel(toString(), onUpdate, decorator);
	}

	private class State {
		BitSet[] checked;
		BitSet[] matched;

		private final int minDistance;
		private final int maxDistance;
		private final List<IvMMove> trace;
		private final IvMFilterTreeNodeComposite<IvMTrace, IvMMove> filters;

		public State(List<IvMMove> trace, IvMFilterTreeNodeComposite<IvMTrace, IvMMove> filters, int minDistance,
				int maxDistance) {
			this.minDistance = minDistance;
			this.maxDistance = maxDistance;
			this.trace = trace;
			this.filters = filters;

			int numberOfChildren = filters.size();

			checked = new BitSet[numberOfChildren];
			matched = new BitSet[numberOfChildren];
			for (int child = 0; child < numberOfChildren; child++) {
				checked[child] = new BitSet();
				matched[child] = new BitSet();
			}
		}

		public boolean tryAll() {
			TIntList childrenToBeTried = new TIntArrayList();
			for (int child = 0; child < filters.size(); child++) {
				childrenToBeTried.add(child);
			}

			//we can match moveIndex to the first child
			for (int moveIndexNext = 0; moveIndexNext < trace.size(); moveIndexNext++) {
				if (tryChild(childrenToBeTried, moveIndexNext)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * 
		 * @param childrenToBeTried
		 * @param moveIndex
		 * @return whether the first child can be matched to moveIndex
		 */
		public boolean tryChild(TIntList childrenToBeTried, int moveIndex) {
			if (childrenToBeTried.isEmpty()) {
				return true;
			}

			int firstChild = childrenToBeTried.get(0);
			if (!matches(firstChild, moveIndex)) {
				return false;
			}

			if (childrenToBeTried.size() == 1) {
				return true;
			}

			if (moveIndex + (childrenToBeTried.size() - 1) * minDistance >= trace.size()) {
				return false;
			}

			TIntList childrenToBeTriedNext = new TIntArrayList(childrenToBeTried);
			childrenToBeTriedNext.removeAt(0);

			//we can match moveIndex to the first child
			for (int moveIndexNext = moveIndex + 1 + minDistance; moveIndexNext < trace.size()
					&& moveIndexNext <= moveIndex + 1 + maxDistance; moveIndexNext++) {
				if (tryChild(childrenToBeTriedNext, moveIndexNext)) {
					return true;
				}
			}
			return false;
		}

		public boolean matches(int childIndex, int moveIndex) {
			if (!checked[childIndex].get(moveIndex)) {
				IvMFilterTreeNode<IvMMove> childFilter = filters.get(childIndex);
				IvMMove move = trace.get(moveIndex);
				matched[childIndex].set(moveIndex, childFilter.staysInLog(move));
				checked[childIndex].set(moveIndex);
			}

			return matched[childIndex].get(moveIndex);
		}
	}

	public IvMFilterTreeNode<IvMTrace> buildFilter(FilterIvMTraceFollowsPanel gui) {
		final int minDistance = gui.getMinimumEventsInBetween();
		final int maxDistance = gui.getMaximumEventsInBetween();

		return new IvMFilterTreeNodeCompositeAbstract<IvMTrace, IvMMove>() {
			private static final long serialVersionUID = -5079650993335491999L;

			public boolean couldSomethingBeFiltered() {
				return true; //the empty trace is always filtered out
			}

			public String getDivider() {
				return "followed after " + minDistance + "-" + maxDistance + " completion events by a completion event";
			}

			public String getPrefix() {
				return "a completion event";
			}

			protected boolean staysInLogA(IvMTrace element) {
				ArrayList<IvMMove> completesTrace = new ArrayList<>(element);
				Iterator<IvMMove> it = completesTrace.iterator();
				while (it.hasNext()) {
					if (!it.next().isComplete()) {
						it.remove();
					}
				}

				State state = new State(completesTrace, this, minDistance, maxDistance);
				return state.tryAll();
			}
		};
	}

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, FilterIvMTraceFollowsPanel gui) {
		int maxTraceLength = (int) attributesInfo.getTraceAttributeValues("number of completion events")
				.getNumericMax();
		gui.setMaxTraceLength(maxTraceLength);
	}
}