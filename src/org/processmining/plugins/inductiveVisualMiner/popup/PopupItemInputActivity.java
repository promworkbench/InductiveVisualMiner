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
		PopupItemInputActivity other = (PopupItemInputActivity) obj;
		if (unode != other.unode) {
			return false;
		}
		return true;
	}

}