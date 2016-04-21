package org.processmining.plugins.inductiveVisualMiner.ivmfilter.filters;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HighlightingFilterQuery {

	public String getName() {
		return "Query filter";
	}

	public IvMFilterGui createGui(XLog log) {
		HighlightingFilterQueryGui panel = new HighlightingFilterQueryGui(getName());
		return panel;
	}

	protected boolean isEnabled() {
		return false;
	}

	public boolean countInColouring(IvMTrace trace) {
		return true;
	}

}
