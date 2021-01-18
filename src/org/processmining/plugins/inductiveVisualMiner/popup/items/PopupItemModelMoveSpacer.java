package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;

public class PopupItemModelMoveSpacer implements PopupItemModelMove {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] {};
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputModelMove> input) {
		return new String[1][0];
	}

}