package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;

public class PopupItemStartEndNumberOfTraces implements PopupItemStartEnd {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputStartEnd> input) {
		if (state.isAlignmentReady()) {
			int value = state.getIvMLogInfoFiltered().getNumberOfTraces();
			return new String[][] { { "number of traces", value + "" } };
		} else {
			return nothing;
		}
	}
}