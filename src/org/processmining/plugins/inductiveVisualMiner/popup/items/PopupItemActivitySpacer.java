package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivitySpacer implements PopupItemActivity {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] {};
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputActivity> input) {
		return new String[1][0];
	}

}