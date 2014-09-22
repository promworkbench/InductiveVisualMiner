package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public class EventAttributeFilter extends ColouringFilter {

	private AttributeFilterGui panel = null;
	private boolean block = false;

	public String getName() {
		return "Event attribute filter";
	}

	public ColouringFilterGui createGui(XLog log) {
		final Map<String, Set<XAttribute>> eventAttributes = getEventAttributeMap(log);
		panel = new AttributeFilterGui(eventAttributes, getName());

		// Key selector
		panel.getKeySelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				block = true;
				String selectedKey = panel.getSelectedKey();

				panel.getAttributeSelector().removeAllItems();
				for (XAttribute a : eventAttributes.get(selectedKey)) {
					panel.getAttributeSelector().addItem(a);
				}
				panel.getAttributeSelector().setSelectedIndex(0);
				block = false;
				update();
			}
		});

		// Attribute value selector
		panel.getAttributeSelector().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!block && e.getStateChange() == ItemEvent.SELECTED) {
					update();
				}
				updateExplanation();
			}
		});

		updateExplanation();
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
		return true;
	}
	
	public void updateExplanation() {
		panel.getExplanation().setText(
				"<html>Include only traces that have at least one event having attribute `" + panel.getSelectedKey() + "' being `"
						+ panel.getSelectedAttribute() + "'</html>");
	}

	private static Map<String, Set<XAttribute>> getEventAttributeMap(XLog log) {
		Map<String, Set<XAttribute>> eventAttributes = new TreeMap<String, Set<XAttribute>>();

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
