package org.processmining.plugins.inductiveVisualMiner.colouringFilter.filters;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class ColouringFilterQuery extends ColouringFilter {

	public String getName() {
		return "Query filter";
	}

	public ColouringFilterGui createGui(XLog log) {
		ColouringFilterQueryGui panel = new ColouringFilterQueryGui(getName());
		return panel;
	}

	protected boolean isEnabled() {
		return false;
	}

	public boolean countInColouring(IvMTrace trace) {
		return true;
	}

}
