package org.processmining.plugins.inductiveVisualMiner.popup;

public class PopupItemInputActivity implements PopupItemInput<PopupItemInputActivity> {
	private final int unode;

	public PopupItemInputActivity(int unode) {
		this.unode = unode;
	}

	public int getUnode() {
		return unode;
	}

	public PopupItemInputActivity get() {
		return this;
	}
}