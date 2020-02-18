package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;

public class PopupItemLogName implements PopupItemLog {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLog> input) {
		if (state.getXLog() != null) {
			XAttribute value = state.getXLog().getAttributes().get("concept:name");
			if (value != null) {
				return new String[][] { //
						{ "name", value.toString() } };

			}
		}
		return nothing;
	}
}