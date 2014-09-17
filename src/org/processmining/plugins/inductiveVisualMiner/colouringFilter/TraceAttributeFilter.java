package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public class TraceAttributeFilter extends ColouringFilter {

	AttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Trace attribute filter";
	}

	public ColouringFilterGui createGui(XLog log) {
		final Map<String, Set<XAttribute>> traceAttributes = getTraceAttributeMap(log);
		panel = new AttributeFilterGui(traceAttributes, getName());

		// Key selector
		panel.getKeySelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				block = true;
				String selectedKey = panel.getSelectedKey();

				panel.getAttributeSelector().removeAllItems();
				for (XAttribute a : traceAttributes.get(selectedKey)) {
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
		String key = panel.getSelectedKey();
		if (!xTrace.getAttributes().containsKey(key)) {
			return false;
		}
		return panel.getSelectedAttribute().equals(xTrace.getAttributes().get(key));
	}

	public boolean isEnabled() {
		return true;
	}

	public void updateExplanation() {
		panel.getExplanation().setText(
				"<html>Include only traces having attribute `" + panel.getSelectedKey() + "' being `"
						+ panel.getSelectedAttribute() + "'</html>");
	}

	private static Map<String, Set<XAttribute>> getTraceAttributeMap(XLog log) {
		Map<String, Set<XAttribute>> traceAttributes = new HashMap<String, Set<XAttribute>>();

		for (XTrace trace : log) {
			for (XAttribute traceAttribute : trace.getAttributes().values()) {
				if (!traceAttributes.containsKey(traceAttribute.getKey())) {
					traceAttributes.put(traceAttribute.getKey(), new TreeSet<XAttribute>());
				}
				traceAttributes.get(traceAttribute.getKey()).add(traceAttribute);
			}
		}
		return traceAttributes;
	}

}
