package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityCost implements PopupItemActivity {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.cost_model };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputActivity> input) {
		CostModel model = inputs.get(IvMObject.cost_model);

		int unode = input.get().getUnode();

		return model.getNodeRepresentationPopup(unode);
	}

}
