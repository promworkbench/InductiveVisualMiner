package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityOccurrences implements PopupItemActivity {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputActivity> input) {
		if (state.isAlignmentReady()) {
			int unode = input.get().getUnode();
			return new String[][] { { //
					"number of occurrences", //
					IvMLogMetrics.getNumberOfTracesRepresented(state.getModel(), unode, false,
							state.getIvMLogInfoFiltered()) + "", //
					} };
		} else {
			return nothing;
		}
	}
}
