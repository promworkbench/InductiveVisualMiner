package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

public class QueueActivityLog {

	private final List<IvMMove> initiateMoves = new ArrayList<>();
	private final List<IvMMove> enqueueMoves = new ArrayList<>();
	private final List<IvMMove> startMoves = new ArrayList<>();
	private final List<IvMMove> completeMoves = new ArrayList<>();
	private final TLongArrayList initiates = new TLongArrayList();
	private final TLongArrayList enqueues = new TLongArrayList();
	private final TLongArrayList starts = new TLongArrayList();
	private final TLongArrayList completes = new TLongArrayList();
	private final TLongArrayList startTraces = new TLongArrayList();
	private final TLongArrayList endTraces = new TLongArrayList();
	private final List<String> resources = new ArrayList<>();
	private final TIntArrayList traceIndices = new TIntArrayList();

	public void add(String resource, Long startTrace, Long initiate, IvMMove initiateMove, Long enqueue,
			IvMMove enqueueMove, Long start, IvMMove startMove, Long complete, IvMMove completeMove, Long endTrace,
			int traceIndex) {
		resources.add(resource);
		traceIndices.add(traceIndex);
		initiateMoves.add(initiateMove);
		enqueueMoves.add(enqueueMove);
		startMoves.add(startMove);
		completeMoves.add(completeMove);
		if (initiate != null) {
			initiates.add(initiate);
		} else {
			initiates.add(-1);
		}
		if (enqueue != null) {
			enqueues.add(enqueue);
		} else {
			enqueues.add(-1);
		}
		if (start != null) {
			starts.add(start);
		} else {
			starts.add(-1);
		}
		if (complete != null) {
			completes.add(complete);
		} else {
			completes.add(-1);
		}
		if (startTrace != null) {
			startTraces.add(startTrace);
		} else {
			startTraces.add(-1);
		}
		if (endTrace != null) {
			endTraces.add(endTrace);
		} else {
			endTraces.add(-1);
		}
	}

	public int size() {
		return resources.size();
	}

	public String getResource(int index) {
		return resources.get(index);
	}

	public boolean hasInitiate(int activityInstanceIndex) {
		return initiates.get(activityInstanceIndex) != -1;
	}

	public boolean hasEnqueue(int activityInstanceIndex) {
		return enqueues.get(activityInstanceIndex) != -1;
	}

	public boolean hasStart(int activityInstanceIndex) {
		return starts.get(activityInstanceIndex) != -1;
	}

	public boolean hasComplete(int activityInstanceIndex) {
		return completes.get(activityInstanceIndex) != -1;
	}

	public boolean hasStartTrace(int activityInstanceIndex) {
		return startTraces.get(activityInstanceIndex) != -1;
	}

	public boolean hasEndTrace(int activityInstanceIndex) {
		return endTraces.get(activityInstanceIndex) != -1;
	}

	public long getInitiate(int activityInstanceIndex) {
		return initiates.get(activityInstanceIndex);
	}

	public IvMMove getInitiateMove(int activityInstanceIndex) {
		return initiateMoves.get(activityInstanceIndex);
	}

	public long getEnqueue(int activityInstanceIndex) {
		return enqueues.get(activityInstanceIndex);
	}

	public IvMMove getEnqueueMove(int activityInstanceIndex) {
		return enqueueMoves.get(activityInstanceIndex);
	}

	public long getStart(int activityInstanceIndex) {
		return starts.get(activityInstanceIndex);
	}

	public IvMMove getStartMove(int activityInstanceIndex) {
		return startMoves.get(activityInstanceIndex);
	}

	public long getComplete(int activityInstanceIndex) {
		return completes.get(activityInstanceIndex);
	}

	public IvMMove getCompleteMove(int activityInstanceIndex) {
		return completeMoves.get(activityInstanceIndex);
	}

	public long getStartTrace(int activityInstanceIndex) {
		return startTraces.get(activityInstanceIndex);
	}

	public long getEndTrace(int activityInstanceIndex) {
		return endTraces.get(activityInstanceIndex);
	}

	public int getTraceIndex(int activityInstanceIndex) {
		return traceIndices.get(activityInstanceIndex);
	}
}
