package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;

import nl.tue.astar.Trace;

/**
 * Callback interface for alignments.
 * 
 * @author sleemans
 *
 */

public interface ETMAlignmentCallback {

	/**
	 * Called each time a trace is finished. Must be thread safe, as may be
	 * called concurrently.
	 * 
	 * @param trace
	 * @param traceAlignment
	 * @param xtracesRepresented
	 */
	public void traceAlignmentComplete(Trace trace, TreeRecord traceAlignment, int[] xtracesRepresented);

	public void alignmentFailed() throws Exception;
}
