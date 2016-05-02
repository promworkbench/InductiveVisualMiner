package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter;

import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

@KeepInProMCache
@HighlightingFilterAnnotation
public abstract class HighlightingFilter extends IvMFilter {

	/**
	 * Main function of the filter. Returns whether the given IvMTrace trace
	 * should be counted towards the result.
	 * 
	 * @param trace
	 * @return
	 */
	public abstract boolean countInColouring(IvMTrace trace);
}
