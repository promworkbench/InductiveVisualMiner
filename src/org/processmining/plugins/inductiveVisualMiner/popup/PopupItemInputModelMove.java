package org.processmining.plugins.inductiveVisualMiner.popup;

public class PopupItemInputModelMove implements PopupItemInput<PopupItemInputModelMove> {
	private final int unode;

	public PopupItemInputModelMove(int unode) {
		this.unode = unode;
	}

	public int getUnode() {
		return unode;
	}

	public PopupItemInputModelMove get() {
		return this;
	}
}