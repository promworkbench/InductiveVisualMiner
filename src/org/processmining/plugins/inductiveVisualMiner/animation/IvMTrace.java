package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.Sextuple;
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

	public ActivityInstanceIterator activityInstanceIterator() {
		return new ActivityInstanceIterator();
	}

	public class ActivityInstanceIterator implements
			Iterator<Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove>> {

		private IvMMove lastSequentialComplete = null;
		private Iterator<IvMMove> it = iterator();

		public boolean hasNext() {
			return it.hasNext();
		}

		/**
		 * Returns the next activity instance. Might return null if the trace is inconsistent.
		 */
		public Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> next() {
			IvMMove enqueue = null;
			IvMMove start = null;

			while (it.hasNext()) {
				IvMMove tMove = it.next();
				if (!tMove.isLogMove()) {

					switch (tMove.getLifeCycleTransition()) {
						case complete :

							//this activity instance is finished
							Sextuple<UnfoldedNode, String, IvMMove, IvMMove, IvMMove, IvMMove> result = Sextuple
									.of(tMove.getUnode(), tMove.getResource(), lastSequentialComplete, enqueue, start,
											tMove);

							//keep track of last sequential complete;
							lastSequentialComplete = tMove;

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

		public void remove() {

		}
	}
}
