package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityOccurrences implements PopupItemActivity {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info_filtered };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputActivity> input) {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo ivmLogInfoFiltered = inputs.get(IvMObject.aligned_log_info_filtered);

		int unode = input.get().getUnode();
		return new String[][] { { //
				"number of occurrences", //
				IvMLogMetrics.getNumberOfTracesRepresented(model, unode, false, ivmLogInfoFiltered) + "", //
				} };
	}
}