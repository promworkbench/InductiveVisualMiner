package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveSpacer implements PopupItemLogMove {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLogMove> input) {
		return new String[1][0];
	}

}