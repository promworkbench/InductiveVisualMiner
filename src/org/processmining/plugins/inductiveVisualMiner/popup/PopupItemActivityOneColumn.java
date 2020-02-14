package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public abstract class PopupItemActivityOneColumn implements PopupItemActivity {

	public boolean isTwoColumns() {
		return false;
	}

	public String[] getColumnA(InductiveVisualMinerState state, int unode) {
		return null;
	}

	public String[] getColumnB(InductiveVisualMinerState state, int unode) {
		return null;
	}
}