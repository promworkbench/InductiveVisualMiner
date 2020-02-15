package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEndOneColumn;

public class PopupItemStartEndEmpty extends PopupItemStartEndOneColumn {

	public String[] getSingleColumn(InductiveVisualMinerState state) {
		return new String[] { null };
	}

}