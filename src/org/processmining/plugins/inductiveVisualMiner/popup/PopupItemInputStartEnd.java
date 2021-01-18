package org.processmining.plugins.inductiveVisualMiner.popup;

public class PopupItemInputStartEnd implements PopupItemInput<PopupItemInputStartEnd> {
	public PopupItemInputStartEnd get() {
		return this;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 1232;
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