package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivityTwoColumn;

public class PopupItemActivityOccurrences extends PopupItemActivityTwoColumn {

	public String[] getColumnA(InductiveVisualMinerState state, int unode) {
		return new String[] { "number of occurrences" };
	}

	public String[] getColumnB(InductiveVisualMinerState state, int unode) {
		if (state.isAlignmentReady()) {
			return new String[] { IvMLogMetrics.getNumberOfTracesRepresented(state.getModel(), unode, false,
					state.getIvMLogInfoFiltered()) + "" };
		} else {
			return nothing;
		}
	}

}
