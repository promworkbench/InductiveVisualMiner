package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public abstract class PopupItemStartEndOneColumn implements PopupItemStartEnd {

	public boolean isTwoColumns() {
		return false;
	}

	public String[] getColumnA(InductiveVisualMinerState state) {
		return null;
	}

	public String[] getColumnB(InductiveVisualMinerState state) {
		return null;
	}
}