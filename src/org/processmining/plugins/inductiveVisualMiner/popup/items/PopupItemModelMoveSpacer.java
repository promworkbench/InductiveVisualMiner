package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;

public class PopupItemModelMoveSpacer implements PopupItemModelMove {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputModelMove> input) {
		return new String[1][0];
	}

}