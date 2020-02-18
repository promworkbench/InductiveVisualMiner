package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityName implements PopupItemActivity {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputActivity> input) {
		int unode = input.get().getUnode();
		return new String[][] { { "Activity " + state.getModel().getActivityName(unode) } };
	}

}
