package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class IvMTrace extends ArrayList<IvMMove> {

	private static final long serialVersionUID = 9214941352493005077L;

	private Double startTime = null;
	private Double endTime = null;
	private String id;

	public IvMTrace(String id) {
		this.id = id;
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

	public String toString() {
		return "[@" + getStartTime() + " " + super.toString() + " " + " @" + getEndTime() + "]";
	}

	public String getId() {
		return id;
	}

	public IvMTrace clone() {
		IvMTrace copy = new IvMTrace(id);
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

	public ActivityInstanceIterator activityInstanceIterator() {
		return new ActivityInstanceIterator();
	}

	public class ActivityInstanceIterator implements
			Iterator<Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove>> {

		private EventIterator it = eventIterator();
		private BitSet visited = new BitSet(size());

		public boolean hasNext() {
			return it.hasNext();
		}

		/**
		 * Returns the next activity instance. Might return null if the trace is
		 * inconsistent.
		 */
		public Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> next() {

			while (it.hasNext()) {
				IvMMove tMove = it.next();
				if (!visited.get(it.getIndexOfLast()) && !tMove.isLogMove()) {
					//we've hit a new activity instance
					UnfoldedNode unode = tMove.getUnode();

					//for initiate, find the last sequential complete
					IvMMove initiate = getLastSequentialComplete(unode);

					//walk through the trace, until the corresponding complete is found
					EventIterator it2 = it.cloneOneBack();
					IvMMove enqueue = null;
					IvMMove start = null;
					while (it2.hasNext()) {
						IvMMove tMove2 = it2.next();

						if (!tMove.isLogMove() && unode.equals(tMove2.getUnode())) {
							visited.set(it2.getIndexOfLast());
							switch (tMove2.getLifeCycleTransition()) {
								case complete :

									//this activity instance is finished
									Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> result = Sextuple
											.of(unode, tMove2.getResource(), initiate, enqueue, start, tMove2);

									//keep track of last sequential complete;
									initiate = tMove;

									return result;
								case other :
									break;
								case start :
									start = tMove;
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

		public IvMMove getLastSequentialComplete(UnfoldedNode unode) {
			EventIterator itBack = it.cloneOneBack();
			while (itBack.hasPrevious()) {
				IvMMove m = itBack.previous();
				if (m.isComplete() && !TreeUtils.areParallel(unode, m.getUnode())) {
					return m;
				}
			}
			return null;
		}

		public void remove() {

		}
	}
}
