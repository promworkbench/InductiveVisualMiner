package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;

public class PopupItemLogName implements PopupItemLog {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.input_log };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputLog> input) {
		XLog log = inputs.get(IvMObject.input_log);

		XAttribute value = log.getAttributes().get("concept:name");
		if (value != null) {
			return new String[][] { //
					{ "name", value.toString() } };

		} else {
			return nothing;
		}
	}
}