package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;

public class PopupItemStartEndName implements PopupItemStartEnd {
	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] {};
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputStartEnd> input) {
		return new String[][] { { "All highlighted traces" } };
	}

}