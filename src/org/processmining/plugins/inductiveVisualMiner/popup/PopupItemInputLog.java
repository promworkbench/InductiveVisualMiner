package org.processmining.plugins.inductiveVisualMiner.popup;

public class PopupItemInputLog implements PopupItemInput<PopupItemInputLog> {
	public PopupItemInputLog get() {
		return this;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 1231;
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
		return true;
	}
}