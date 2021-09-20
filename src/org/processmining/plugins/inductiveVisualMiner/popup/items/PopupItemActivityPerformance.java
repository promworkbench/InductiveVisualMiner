package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.performance.Aggregate;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceUtils;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityPerformance implements PopupItemActivity {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.performance };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputActivity> input) {
		Performance performance = inputs.get(IvMObject.performance);

		int unode = input.get().getUnode();
		String[][] result = new String[DurationType.values().length * (Aggregate.values().length + 1)][];

		int i = 0;
		for (DurationType type : DurationType.values()) {
			for (Aggregate gather : Aggregate.values()) {
				long m = performance.getNodeMeasure(type, gather, unode);
				if (m > -1) {
					result[i] = new String[] { //
							gather.toString() + " " + type.toString(), //
							PerformanceUtils.timeToString(m) //
					};
					i++;
				}
			}
			result[i] = new String[0];
			i++;
		}
		return result;
	}

}
