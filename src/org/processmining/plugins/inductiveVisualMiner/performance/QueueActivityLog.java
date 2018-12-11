package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TLongArrayList;

public class QueueActivityLog {

	private final TLongArrayList moves = new TLongArrayList();
	private final List<String> resources = new ArrayList<>();

	public void add(String resource, Long startTrace, Long initiate, Long enqueue, Long start, Long complete,
			Long endTrace) {
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
		if (startTrace != null) {
			moves.add(startTrace);
		} else {
			moves.add(-1);
		}
		if (endTrace != null) {
			moves.add(endTrace);
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

	public boolean hasInitiate(int traceIndex) {
		return moves.get(traceIndex * 6) != -1;
	}

	public boolean hasEnqueue(int traceIndex) {
		return moves.get(traceIndex * 6 + 1) != -1;
	}

	public boolean hasStart(int traceIndex) {
		return moves.get(traceIndex * 6 + 2) != -1;
	}

	public boolean hasComplete(int traceIndex) {
		return moves.get(traceIndex * 6 + 3) != -1;
	}

	public boolean hasStartTrace(int traceIndex) {
		return moves.get(traceIndex * 6 + 4) != -1;
	}

	public boolean hasEndTrace(int traceIndex) {
		return moves.get(traceIndex * 6 + 5) != -1;
	}

	public long getInitiate(int traceIndex) {
		return moves.get(traceIndex * 6);
	}

	public long getEnqueue(int traceIndex) {
		return moves.get(traceIndex * 6 + 1);
	}

	public long getStart(int traceIndex) {
		return moves.get(traceIndex * 6 + 2);
	}

	public long getComplete(int traceIndex) {
		return moves.get(traceIndex * 6 + 3);
	}

	public long getStartTrace(int traceIndex) {
		return moves.get(traceIndex * 6 + 4);
	}

	public long getEndTrace(int traceIndex) {
		return moves.get(traceIndex * 6 + 5);
	}
}
