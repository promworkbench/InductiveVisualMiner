package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public class EventAttributeFilter extends ColouringFilter {

	EventAttributeFilterGui panel = null;

	public String getName() {
		return "Event attribute filter";
	}

	public ColouringFilterGui createGui(XLog log) {
		final Map<String, Set<XAttribute>> eventAttributes = getEventAttributeMap(log);
		panel = new EventAttributeFilterGui(eventAttributes);

		// Key selector
		panel.getKeySelector().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String selectedKey = panel.getSelectedKey();

				panel.getAttributeSelector().removeAllItems();
				for (XAttribute a : eventAttributes.get(selectedKey)) {
					panel.getAttributeSelector().addItem(a);
				}
				panel.getAttributeSelector().setSelectedIndex(0);

				update();
			}
		});

		// Attribute value selector
		panel.getAttributeSelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});

		panel.getEnabled().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});

		return panel;
	}

	public boolean countInColouring(XTrace xTrace, AlignedTrace aTrace) {
		XAttribute attribute = panel.getSelectedAttribute();
		String key = panel.getSelectedKey();
		for (XEvent xEvent : xTrace) {
			if (!xEvent.getAttributes().containsKey(key)) {
				continue;
			}
			if (attribute.equals(xEvent.getAttributes().get(key))) {
				return true;
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return panel.getEnabled().isSelected();
	}

	private static Map<String, Set<XAttribute>> getEventAttributeMap(XLog log) {
		Map<String, Set<XAttribute>> eventAttributes = new HashMap<String, Set<XAttribute>>();

		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (XAttribute eventAttribute : event.getAttributes().values()) {
					if (!eventAttributes.containsKey(eventAttribute.getKey())) {
						eventAttributes.put(eventAttribute.getKey(), new TreeSet<XAttribute>());
					}
					eventAttributes.get(eventAttribute.getKey()).add(eventAttribute);
				}
			}
		}
		return eventAttributes;
	}

}
