package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeComposite;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FilterIMTraceFollows extends IvMFilterBuilderAbstract<IMTrace, XEvent, FilterIvMTraceFollowsPanel> {

	public String toString() {
		return "follows";
	}

	public String toString(FilterIvMTraceFollowsPanel panel) {
		return "follows";
	}

	public boolean allowsChildren() {
		return true;
	}

	public Class<IMTrace> getTargetClass() {
		return IMTrace.class;
	}

	public Class<XEvent> getChildrenTargetClass() {
		return XEvent.class;
	}

	public FilterIvMTraceFollowsPanel createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		return new FilterIvMTraceFollowsPanel(toString(), onUpdate, decorator);
	}

	private class State {
		BitSet[] checked;
		BitSet[] matched;

		private final int minDistance;
		private final int maxDistance;
		private final List<XEvent> trace;
		private final IvMFilterTreeNodeComposite<IMTrace, XEvent> filters;

		public State(List<XEvent> trace, IvMFilterTreeNodeComposite<IMTrace, XEvent> filters, int minDistance,
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
				IvMFilterTreeNode<XEvent> childFilter = filters.get(childIndex);
				XEvent move = trace.get(moveIndex);
				matched[childIndex].set(moveIndex, childFilter.staysInLog(move));
				checked[childIndex].set(moveIndex);
			}

			return matched[childIndex].get(moveIndex);
		}
	}

	public IvMFilterTreeNode<IMTrace> buildFilter(FilterIvMTraceFollowsPanel gui) {
		final int minDistance = gui.getMinimumEventsInBetween();
		final int maxDistance = gui.getMaximumEventsInBetween();

		return new IvMFilterTreeNodeCompositeAbstract<IMTrace, XEvent>() {
			private static final long serialVersionUID = -5079650993335491999L;

			public boolean couldSomethingBeFiltered() {
				return true; //the empty trace is always filtered out
			}

			public String getDivider() {
				return "followed after " + minDistance + "-" + maxDistance + " events by an event";
			}

			public String getPrefix() {
				return "an event";
			}

			protected boolean staysInLogA(IMTrace element) {
				List<XEvent> trace = new ArrayList<>();
				for (XEvent event : element) {
					trace.add(event);
				}

				State state = new State(trace, this, minDistance, maxDistance);
				return state.tryAll();
			}
		};
	}

	public void setAttributesInfo(AttributesInfo attributesInfo, FilterIvMTraceFollowsPanel gui) {
		int maxTraceLength = (int) attributesInfo.getTraceAttributeValues("number of events").getNumericMax();
		gui.setMaxTraceLength(maxTraceLength);
	}
}