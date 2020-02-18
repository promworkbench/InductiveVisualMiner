package org.processmining.plugins.inductiveVisualMiner.popup.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;

public class PopupItemLogAttributes implements PopupItemLog {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLog> input) {
		if (state.getAttributesInfo() != null) {
			AttributesInfo info = state.getAttributesInfo();
			String[][] result = new String[info.getEventAttributes().size() + info.getTraceAttributes().size()][];
			int i = 0;

			//trace attributes
			{
				String traceTitle = info.getTraceAttributes().size() == 1 ? "trace attribute" : "trace attributes";
				ArrayList<Attribute> attributes = new ArrayList<>(info.getTraceAttributes());
				Collections.sort(attributes, new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
					}
				});
				for (Attribute attribute : attributes) {
					result[i] = new String[] { //
							(i == 0 ? traceTitle : ""), //
							attribute.getName() //
					};
					i++;
				}
			}

			//event attributes
			{
				String eventTitle = info.getEventAttributes().size() == 1 ? "event attribute" : "event attributes";
				ArrayList<Attribute> attributes = new ArrayList<>(info.getEventAttributes());
				Collections.sort(attributes, new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
					}
				});
				int start = i;
				for (Attribute attribute : attributes) {
					result[i] = new String[] { //
							(i == start ? eventTitle : ""), //
							attribute.getName() //
					};
					i++;
				}
			}

			return result;
		}
		return nothing;
	}

}
