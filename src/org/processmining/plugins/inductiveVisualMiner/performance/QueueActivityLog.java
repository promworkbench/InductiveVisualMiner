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
	
	private Long mask(long result) {
		if (result < 0) {
			return null;
		}
		return result;
	}

	public Long getInitiate(int index) {
		return mask(moves.get(index * 4));
	}

	public Long getEnqueue(int index) {
		return mask(moves.get(index * 4 + 1));
	}

	public Long getStart(int index) {
		return mask(moves.get(index * 4 + 2));
	}

	public Long getComplete(int index) {
		return mask(moves.get(index * 4 + 3));
	}
}
