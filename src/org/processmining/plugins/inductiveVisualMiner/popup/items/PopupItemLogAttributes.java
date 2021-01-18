package org.processmining.plugins.inductiveVisualMiner.popup.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PopupItemLogAttributes implements PopupItemLog {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.attributes_info };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputLog> input) {
		AttributesInfo info = inputs.get(IvMObject.attributes_info);

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

}
