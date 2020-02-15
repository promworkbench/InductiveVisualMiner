package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEndOneColumn;

public class PopupItemStartEndName extends PopupItemStartEndOneColumn {

	public String[] getSingleColumn(InductiveVisualMinerState state) {
		return new String[] { "all highlighted traces" };
	}

}