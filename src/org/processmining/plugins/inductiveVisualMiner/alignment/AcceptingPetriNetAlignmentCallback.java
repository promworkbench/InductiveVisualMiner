package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.SortedSet;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public interface AcceptingPetriNetAlignmentCallback {
	/**
	 * Called each time a trace is finished. Must be thread safe, as may be
	 * called concurrently.
	 * 
	 * @param aTrace
	 * @param xTraces
	 * @param eventClasses
	 */
	public void traceAlignmentComplete(SyncReplayResult aTrace, SortedSet<Integer> xTraces,
			IvMEventClasses eventClasses);

	public void alignmentFailed() throws Exception;

	public IvMLogNotFiltered getAlignedLog();
}
