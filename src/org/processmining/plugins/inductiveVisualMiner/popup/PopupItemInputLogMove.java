package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;

public class PopupItemInputLogMove implements PopupItemInput<PopupItemInputLogMove> {

	private final LogMovePosition position;

	public PopupItemInputLogMove(LogMovePosition position) {
		this.position = position;
	}

	public PopupItemInputLogMove get() {
		return this;
	}

	public LogMovePosition getPosition() {
		return position;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		PopupItemInputLogMove other = (PopupItemInputLogMove) obj;
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		return true;
	}

}