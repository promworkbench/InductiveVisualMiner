package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

public class QueueActivityLog {

	private final List<IvMMove> completeMoves = new ArrayList<>();
	private final List<IvMMove> initiateMoves = new ArrayList<>();
	private final TLongArrayList moves = new TLongArrayList();
	private final List<String> resources = new ArrayList<>();
	private final TIntArrayList traceIndices = new TIntArrayList();

	public void add(String resource, Long startTrace, Long initiate, IvMMove initiateMove, Long enqueue, Long start,
			Long complete, IvMMove completeMove, Long endTrace, int traceIndex) {
		resources.add(resource);
		traceIndices.add(traceIndex);
		initiateMoves.add(initiateMove);
		completeMoves.add(completeMove);
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

	public boolean hasInitiate(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6) != -1;
	}

	public boolean hasEnqueue(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 1) != -1;
	}

	public boolean hasStart(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 2) != -1;
	}

	public boolean hasComplete(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 3) != -1;
	}

	public boolean hasStartTrace(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 4) != -1;
	}

	public boolean hasEndTrace(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 5) != -1;
	}

	public long getInitiate(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6);
	}

	public IvMMove getInitiateMove(int activityInstanceIndex) {
		return initiateMoves.get(activityInstanceIndex);
	}

	public long getEnqueue(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 1);
	}

	public long getStart(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 2);
	}

	public long getComplete(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 3);
	}

	public IvMMove getCompleteMove(int activityInstanceIndex) {
		return completeMoves.get(activityInstanceIndex);
	}

	public long getStartTrace(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 4);
	}

	public long getEndTrace(int activityInstanceIndex) {
		return moves.get(activityInstanceIndex * 6 + 5);
	}

	public int getTraceIndex(int activityInstanceIndex) {
		return traceIndices.get(activityInstanceIndex);
	}
}
