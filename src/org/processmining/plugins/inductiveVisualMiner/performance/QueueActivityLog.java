package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;

public class QueueActivityLog {

	private final TLongArrayList moves = new TLongArrayList();
	private final List<String> resources = new ArrayList<>();

	public void add(String resource, Long initiate, Long enqueue, Long start, Long complete) {
		resources.add(resource);
		if (initiate != null) {
			moves.add(initiate);
		} else {
			moves.add(-1);
		}
		if (enqueue != null) {
			moves.add(enqueue);
		} else {
			moves.add(-1);
		}
		if (start != null) {
			moves.add(start);
		} else {
			moves.add(-1);
		}
		if (complete != null) {
			moves.add(complete);
		} else {
			moves.add(-1);
		}
	}
	
	public int size() {
		return resources.size();
	}

	public String getResource(int index) {
		return resources.get(index);
	}

	public long getInitiate(int traceIndex) {
		return moves.get(traceIndex * 4);
	}

	public long getEnqueue(int traceIndex) {
		return moves.get(traceIndex * 4 + 1);
	}

	public long getStart(int traceIndex) {
		return moves.get(traceIndex * 4 + 2);
	}

	public long getComplete(int traceIndex) {
		return moves.get(traceIndex * 4 + 3);
	}
}
