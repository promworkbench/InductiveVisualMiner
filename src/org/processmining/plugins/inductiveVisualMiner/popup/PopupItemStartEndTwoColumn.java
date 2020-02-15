package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public abstract class PopupItemStartEndTwoColumn implements PopupItemStartEnd {

	public static final String[] nothing = new String[0];

	public boolean isTwoColumns() {
		return true;
	}

	public String[] getSingleColumn(InductiveVisualMinerState state) {
		return null;
	}

}