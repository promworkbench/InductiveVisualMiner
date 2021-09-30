package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.performance.Aggregate;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceLevel.Level;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceUtils;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;

public class PopupItemStartEndPerformance implements PopupItemStartEnd {
	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.performance };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputStartEnd> input) {
		Performance performance = inputs.get(IvMObject.performance);

		String[][] result = new String[DurationType.valuesAt(Level.process).length * (Aggregate.values().length + 1)][];

		int i = 0;
		for (DurationType type : DurationType.valuesAt(Level.process)) {
			for (Aggregate gather : Aggregate.valuesForPopups()) {
				long m = performance.getProcessMeasure(type, gather);
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