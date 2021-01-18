package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;

public class PopupItemLogTitle implements PopupItemLog {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] {};
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputLog> input) {
		return new String[][] { { "Log information" } };
	}
}