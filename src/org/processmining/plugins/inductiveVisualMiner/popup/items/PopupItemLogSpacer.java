package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;

public class PopupItemLogSpacer implements PopupItemLog {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLog> input) {
		return new String[1][0];
	}

}