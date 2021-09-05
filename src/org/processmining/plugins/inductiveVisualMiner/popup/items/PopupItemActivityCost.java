package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityCost implements PopupItemActivity {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_cost_models };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputActivity> input) {
		CostModel model = inputs.get(IvMObject.data_analysis_cost_models).getCostModel();

		if (model == null) {
			return new String[0][0];
		}

		int unode = input.get().getUnode();

		return model.getNodeRepresentationPopup(unode);
	}

}
