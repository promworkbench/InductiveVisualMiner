package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public class TraceAttributeFilter extends ColouringFilter {

	TraceAttributeFilterGui panel = null;

	public String getName() {
		return "Trace attribute filter";
	}

	public ColouringFilterGui createGui(XLog log) {
		final Map<String, Set<XAttribute>> traceAttributes = getTraceAttributeMap(log);
		panel = new TraceAttributeFilterGui(traceAttributes);

		// Key selector
		panel.getKeySelector().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String selectedKey = panel.getSelectedKey();

				panel.getAttributeSelector().removeAllItems();
				for (XAttribute a : traceAttributes.get(selectedKey)) {
					panel.getAttributeSelector().addItem(a);
				}
				panel.getAttributeSelector().setSelectedIndex(0);

				update();
			}
		});
		//		keySelector.setSelectedIndex(0); // based on that concept:name always as trace attribute
		//		panel.add(keySelector);

		// Attribute value selector
		panel.getAttributeSelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		//		panel.add(attributeSelector);
		//		attributeSelector.setSelectedIndex(0);

		//		enabled = new JCheckBox();
		panel.getEnabled().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		//		panel.add(enabled);

		return panel;
	}

	public boolean countInColouring(XTrace xTrace, AlignedTrace aTrace) {
		XAttribute attribute = panel.getSelectedAttribute();
		if (!xTrace.getAttributes().containsKey(panel.getSelectedKey())) {
			return false;
		}
		// TODO: test equals function
		return attribute.equals(panel.getSelectedAttribute());
	}

	public boolean isEnabled() {
		return panel.getEnabled().isSelected();
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
