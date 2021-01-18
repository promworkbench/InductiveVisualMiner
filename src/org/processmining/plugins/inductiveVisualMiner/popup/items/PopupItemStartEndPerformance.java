package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeGlobal;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;

public class PopupItemStartEndPerformance implements PopupItemStartEnd {
	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.performance };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputStartEnd> input) {
		PerformanceWrapper performance = inputs.get(IvMObject.performance);

		String[][] result = new String[TypeGlobal.size * (Gather.size + 1)][];

		int i = 0;
		for (TypeGlobal type : TypeGlobal.values()) {
			for (Gather gather : Gather.values()) {
				long m = performance.getGlobalMeasure(type, gather);
				if (m > -1) {
					result[i] = new String[] { //
							gather.toString() + " " + type.toString(), //
							Performance.timeToString(m) //
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