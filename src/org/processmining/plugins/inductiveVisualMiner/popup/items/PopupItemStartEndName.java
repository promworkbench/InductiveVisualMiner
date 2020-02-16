package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;

public class PopupItemStartEndName implements PopupItemStartEnd {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputStartEnd> input) {
		return new String[][] { { "all highlighted traces" } };
	}

}