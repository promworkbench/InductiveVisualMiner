package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.BitSet;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.IvMLogFilter;

public class IvMLogImplFiltered implements IvMLog, IvMLogFilter {

	private final BitSet traceIsFilteredOut;
	private final IvMLogNotFiltered log;
	private boolean somethingFiltered;

	public IvMLogImplFiltered(IvMLogNotFiltered log) {
		this.log = log;
		traceIsFilteredOut = new BitSet(log.size());
		somethingFiltered = false;
	}

	/**
	 * Get an iterator over all traces that are not filtered out.
	 * 
	 * @return
	 */
	public IteratorWithPosition<IvMTrace> iterator() {
		return new IteratorWithPosition<IvMTrace>() {
			int next = init();
			int now = next - 1;

			private int init() {
				//start with normal traces
				return traceIsFilteredOut.nextClearBit(0) < log.size() ? traceIsFilteredOut.nextClearBit(0) : log
						.size();
			}

			public boolean hasNext() {
				return next < log.size();
			}

			public void remove() {
				traceIsFilteredOut.set(now);
				somethingFiltered = true;
			}

			public IvMTrace next() {
				now = next;
				next = traceIsFilteredOut.nextClearBit(next + 1);
				return log.get(now);
			}

			public int getPosition() {
				return now;
			}
		};
	}

	/**
	 * 
	 * @return whether at least one trace is filtered out.
	 */
	public boolean isSomethingFiltered() {
		return somethingFiltered;
	}

	public boolean isFilteredOut(int traceIndex) {
		return traceIsFilteredOut.get(traceIndex);
	}
}
