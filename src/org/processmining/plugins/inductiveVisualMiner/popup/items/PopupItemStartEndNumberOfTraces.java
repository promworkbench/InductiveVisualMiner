package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEndTwoColumn;

public class PopupItemStartEndNumberOfTraces extends PopupItemStartEndTwoColumn {

	public String[] getColumnA(InductiveVisualMinerState state) {
		if (state.isAlignmentReady() && state.getIvMLogFiltered() != null) {
			return new String[] { "number of traces" };
		} else {
			return nothing;
		}
	}

	public String[] getColumnB(InductiveVisualMinerState state) {
		if (state.isAlignmentReady() && state.getIvMLogFiltered() != null) {
			return new String[] { state.getIvMLogInfoFiltered().getNumberOfTraces() + "" };
		} else {
			return nothing;
		}
	}
}