package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public abstract class HighlightingFilter extends IvMFilter {

	/**
	 * Main function of the filter. Returns whether the given IvMTrace trace
	 * should be counted towards the result.
	 * 
	 * @param trace
	 * @return
	 */
	public abstract boolean staysInLog(IvMTrace trace);

	/**
	 * Update the filter with the attributes info.
	 * 
	 * @param attributesInfo
	 */
	public abstract void setAttributesInfo(IvMAttributesInfo attributesInfo);
}