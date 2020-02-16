package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;

public class PopupItemStartEndPerformance implements PopupItemStartEnd {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputStartEnd> input) {
		return nothing;
	}

	//					//times
	//					if (state.isPerformanceReady()) {
	//						for (TypeGlobal type : TypeGlobal.values()) {
	//							for (Gather gather : Gather.values()) {
	//								long m = state.getPerformance().getGlobalMeasure(type, gather);
	//								if (m > -1) {
	//									popup.add(gather.toString() + " " + type.toString() + " "
	//											+ Performance.timeToString(m));
	//								}
	//							}
	//							if (popup.get(popup.size() - 1) != null) {
	//								popup.add(null);
	//							}
	//						}
	//					}
}
