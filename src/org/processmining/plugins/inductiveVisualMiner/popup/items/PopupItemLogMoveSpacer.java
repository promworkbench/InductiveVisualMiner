package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveSpacer implements PopupItemLogMove {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] {};
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputLogMove> input) {
		return new String[1][0];
	}

}