package org.processmining.plugins.inductiveVisualMiner.highlightingfilter.filters;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.highlightingfilter.HighlightingFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HighlightingFilterQuery {

	public String getName() {
		return "Query filter";
	}

	public HighlightingFilterGui createGui(XLog log) {
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
