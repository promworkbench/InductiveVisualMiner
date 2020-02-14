package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivityTwoColumn;

public class PopupItemActivityOccurrencesPerTrace extends PopupItemActivityTwoColumn {

	public String[] getColumnA(InductiveVisualMinerState state, int unode) {
		return new String[] { "occurrences per trace" };
	}

	public String[] getColumnB(InductiveVisualMinerState state, int unode) {
		if (state.isAlignmentReady()) {
			return new String[] { (IvMLogMetrics.getNumberOfTracesRepresented(state.getModel(), unode, false,
					state.getIvMLogInfoFiltered()) / (state.getIvMLogInfoFiltered().getNumberOfTraces() * 1.0)) + "" };
		} else {
			return nothing;
		}
	}
}