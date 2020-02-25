package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import org.deckfour.xes.model.XAttributeMap;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;

public class IvMTraceImpl extends ArrayList<IvMMove> implements IvMTrace {

	private static final long serialVersionUID = 9214941352493005077L;

	private Double startTime = null;
	private Double endTime = null;
	private Long realStartTime = null;
	private Long realEndTime = null;
	private String name;
	private final XAttributeMap attributes;

	public IvMTraceImpl(String name, XAttributeMap attributes, int size) {
		super(size);
		this.name = name;
		this.attributes = attributes;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public Double getEndTime() {
		return endTime;
	}

	public Double getStartTime() {
		return startTime;
	}

	public void setRealEndTime(long endTime) {
		this.realEndTime = endTime;
	}

	public void setRealStartTime(long startTime) {
		this.realStartTime = startTime;
	}

	public Long getRealEndTime() {
		return realEndTime;
	}

	public Long getRealStartTime() {
		return realStartTime;
	}

	public String toString() {
		return "[@" + getStartTime() + " " + super.toString() + " " + " @" + getEndTime() + "]";
	}

	public String getName() {
		return name;
	}

	public XAttributeMap getAttributes() {
		return attributes;
	}

	public IvMTrace clone() {
		IvMTrace copy = new IvMTraceImpl(name, attributes, size());
		copy.addAll(this);
		copy.setStartTime(startTime);
		copy.setEndTime(endTime);
		return copy;
	}

	public EventIterator eventIterator() {
		return new EventIterator();
	}

	public class EventIterator implements Iterator<IvMMove> {
		private int i = 0;

		public boolean hasNext() {
			return i < size();
		}

		public IvMMove next() {
			i++;
			return get(i - 1);
		}

		public boolean hasPrevious() {
			return i > 0;
		}

		public IvMMove previous() {
			i--;
			return get(i);
		}

		public void remove() {

		}

		public int getIndexOfLast() {
			return i - 1;
		}

		public EventIterator cloneOneBack() {
			EventIterator result = new EventIterator();
			result.i = i - 1;
			return result;
		}
	}

	public ActivityInstanceIterator activityInstanceIterator(IvMModel model) {
		return new ActivityInstanceIterator(model);
	}

	public class ActivityInstanceIterator
			implements Iterator<Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove>> {

		private EventIterator it = eventIterator();
		private BitSet visited = new BitSet(size());
		private IvMModel model;

		public ActivityInstanceIterator(IvMModel model) {
			this.model = model;
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		/**
		 * Returns the next activity instance. Might return null if the trace is
		 * inconsistent. Sextuple of:<br>
		 * - activity<br>
		 * - resource<br>
		 * - initiate move<br>
		 * - enqueue move<br>
		 * - start move<br>
		 * - complete move<br>
		 */
		public Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> next() {

			while (it.hasNext()) {
				IvMMove tMove = it.next();
				if (!visited.get(it.getIndexOfLast()) && !tMove.isLogMove()) {
					//we've hit a new activity instance
					int unode = tMove.getTreeNode();

					//for initiate, find the last sequential complete
					IvMMove initiate = getLastSequentialComplete(model, unode);

					//walk through the trace, until the corresponding complete is found
					EventIterator it2 = it.cloneOneBack();
					IvMMove enqueue = null;
					IvMMove start = null;
					while (it2.hasNext()) {
						IvMMove tMove2 = it2.next();

						if (!tMove.isLogMove() && unode == tMove2.getTreeNode()) {
							visited.set(it2.getIndexOfLast());
							switch (tMove2.getLifeCycleTransition()) {
								case complete :

									//this activity instance is finished
									Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> result = Sextuple
											.of(unode, tMove2.getResource(), initiate, enqueue, start, tMove2);

									return result;
								case start :
									start = tMove2;
									break;
								case enqueue :
									enqueue = tMove2;
									break;
								case other :
									break;
							}
						}
					}
					//inconsistent trace, as this trace does not end with complete.
					return null;
				}
			}

			//inconsistent trace, as this trace does not end with complete.
			return null;
		}

		public IvMMove getLastSequentialComplete(IvMModel model, int unode) {
			EventIterator itBack = it.cloneOneBack();
			while (itBack.hasPrevious()) {
				IvMMove m = itBack.previous();
				if (model.isTree()) {
					if (m.isModelSync() && !(model.isTau(m.getTreeNode())) && m.isComplete()
							&& !TreeUtils.areParallel(model.getTree().getUnfoldedNode(unode),
									model.getTree().getUnfoldedNode(m.getTreeNode()))) {
						return m;
					}
				} else {
					//in a dfg, every two nodes are sequential
					if (m.isModelSync() && m.isComplete()) {
						return m;
					}
				}
			}
			return null;
		}

		public void remove() {

		}
	}

	public int getNumberOfEvents() {
		int result = 0;
		for (IvMMove e : this) {
			if (!e.isModelMove() && e.getActivityEventClass() != null) {
				result++;
			}
		}
		return result;
	}
}
