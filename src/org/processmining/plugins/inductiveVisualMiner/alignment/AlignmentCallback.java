package org.processmining.plugins.inductiveVisualMiner.alignment;

import nl.tue.astar.Trace;

import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;

/**
 * Callback interface for alignments.
 * 
 * @author sleemans
 *
 */

public interface AlignmentCallback {

	public void traceAlignmentComplete(Trace trace, TreeRecord traceAlignment, int[] xtracesRepresented);

	public void alignmentFailed() throws Exception;
}
