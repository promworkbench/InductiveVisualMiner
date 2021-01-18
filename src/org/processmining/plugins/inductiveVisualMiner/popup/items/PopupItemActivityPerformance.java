package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;

public class PopupItemActivityPerformance implements PopupItemActivity {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.performance };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputActivity> input) {
		PerformanceWrapper performance = inputs.get(IvMObject.performance);

		int unode = input.get().getUnode();
		String[][] result = new String[TypeNode.size * (Gather.size + 1)][];

		int i = 0;
		for (TypeNode type : TypeNode.values()) {
			for (Gather gather : Gather.values()) {
				long m = performance.getNodeMeasure(type, gather, unode);
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
