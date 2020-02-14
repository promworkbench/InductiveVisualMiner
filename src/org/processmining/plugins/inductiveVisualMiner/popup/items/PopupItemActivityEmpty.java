package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivityOneColumn;

public class PopupItemActivityEmpty extends PopupItemActivityOneColumn {

	public String[] getSingleColumn(InductiveVisualMinerState state, int unode) {
		return new String[] { null };
	}

}