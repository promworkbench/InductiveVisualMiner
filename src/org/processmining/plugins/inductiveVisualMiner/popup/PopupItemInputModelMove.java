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

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + unode;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PopupItemInputModelMove other = (PopupItemInputModelMove) obj;
		if (unode != other.unode) {
			return false;
		}
		return true;
	}

}