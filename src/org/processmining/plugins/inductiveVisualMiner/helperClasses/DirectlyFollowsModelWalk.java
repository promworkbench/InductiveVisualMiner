package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public abstract class DirectlyFollowsModelWalk {
	public void walk(DirectlyFollowsModel dfm, IvMTrace trace) {
		boolean traceIsEmpty = true;
		for (int eventIndex = 0; eventIndex < trace.size(); eventIndex++) {
			//only model moves
			IvMMove move = trace.get(eventIndex);

			if (move.isModelSync()) {
				traceIsEmpty = false;

				int firstEventIndex = eventIndex;

				nodeEntered(trace, move.getTreeNode(), firstEventIndex);

				int lastEventIndex = getLastEventOfNode(trace, firstEventIndex);

				nodeExecuted(trace, move.getTreeNode(), firstEventIndex, lastEventIndex);

				eventIndex = lastEventIndex;
			}
		}

		if (traceIsEmpty) {
			emptyTraceExecuted(trace);
		}
	}

	private int getLastEventOfNode(IvMTrace trace, int firstEventIndex) {
		int eventNode = trace.get(firstEventIndex).getTreeNode();
		int eventNodeLastSeen = firstEventIndex;

		for (int i = firstEventIndex + 1; i < trace.size(); i++) {
			IvMMove move2 = trace.get(i);
			if (move2.isModelSync()) {
				if (move2.getTreeNode() != eventNode) {
					return eventNodeLastSeen;
				} else {
					eventNodeLastSeen = i;
				}
			}
		}

		return trace.size() - 1;

	}

	/**
	 * Called whenever a node is entered.
	 * 
	 * @param trace
	 * @param node
	 * @param eventIndex
	 */
	public abstract void nodeEntered(IvMTrace trace, int node, int eventIndex);

	/**
	 * Called whenever the execution of a node is completed.
	 * 
	 * @param trace
	 * 
	 * @param node
	 * @param startEventIndex
	 *            (inclusive)
	 * @param lastEventIndex
	 *            (inclusive)
	 */
	public abstract void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex);

	public abstract void emptyTraceExecuted(IvMTrace trace);
}