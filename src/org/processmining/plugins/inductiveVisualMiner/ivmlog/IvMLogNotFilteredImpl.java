package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import org.deckfour.xes.model.XAttributeMap;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;

public class IvMLogNotFilteredImpl implements IvMLogNotFiltered {

	private final IvMTrace[] traces;
	private final XAttributeMap attributes;

	public IvMLogNotFilteredImpl(int numberOfTraces, XAttributeMap attributes) {
		traces = new IvMTrace[numberOfTraces];
		this.attributes = attributes;
	}

	public IteratorWithPosition<IvMTrace> iterator() {
		return new IteratorWithPosition<IvMTrace>() {
			int now = -1;

			public boolean hasNext() {
				return now + 1 < traces.length;
			}

			public void remove() {
				throw new RuntimeException("removal of traces is not permitted");
			}

			public IvMTrace next() {
				now = now + 1;
				return traces[now];
			}

			public int getPosition() {
				return now;
			}
		};
	}

	public IvMTrace get(int traceIndex) {
		return traces[traceIndex];
	}

	public void set(int traceIndex, IvMTrace trace) {
		traces[traceIndex] = trace;
	}

	public int size() {
		return traces.length;
	}

	public XAttributeMap getAttributes() {
		return attributes;
	}

}
