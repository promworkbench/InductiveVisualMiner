package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public abstract class PreMiningFilter extends IvMFilter {
	/**
	 * Update the filter with the attributes info.
	 * 
	 * @param attributesInfo
	 */
	public abstract void setAttributesInfo(AttributesInfo attributesInfo);
}