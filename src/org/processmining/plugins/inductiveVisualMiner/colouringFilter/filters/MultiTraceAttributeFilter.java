package org.processmining.plugins.inductiveVisualMiner.colouringFilter.filters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilterGui;

public class MultiTraceAttributeFilter extends ColouringFilter {

	MultiAttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Trace attribute filter";
	}

	public ColouringFilterGui createGui(XLog log) {
		final Map<String, Set<XAttribute>> traceAttributes = getTraceAttributeMap(log);
		panel = new MultiAttributeFilterGui(traceAttributes, getName());

		// Key selector
		panel.getKeySelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				block = true;
				String selectedKey = panel.getSelectedKey();

				panel.replaceAttributes(traceAttributes.get(selectedKey));
				block = false;
				update();
			}
		});

		// Attribute value selector
		panel.getAttributeSelector().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!block) {
					update();
				}
				updateExplanation();
			}
		});

		block = false;
		updateExplanation();
		return panel;
	}

	public boolean countInColouring(IMTrace trace, AlignedTrace aTrace) {
		String key = panel.getSelectedKey();
		if (!trace.getAttributes().containsKey(key)) {
			return false;
		}
		return panel.getSelectedAttributes().contains(trace.getAttributes().get(key));
	}

	public boolean isEnabled() {
		return !panel.getSelectedAttributes().isEmpty();
	}

	public void updateExplanation() {
		if (panel.getSelectedAttributes().isEmpty()) {
			panel.getExplanation().setText("<html>Include only traces having an attribute as selected.</html>");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("<html>Include only traces having attribute `");
			s.append(panel.getSelectedKey());
			s.append("' being ");
			List<XAttribute> attributes = panel.getSelectedAttributes();
			if (attributes.size() > 1) {
				s.append("either ");
			}
			for (int i = 0; i < attributes.size(); i++) {
				s.append("`");
				s.append(attributes.get(i));
				s.append("'");
				if (i == attributes.size() - 2) {
					s.append(" or ");
				} else if (i < attributes.size() - 1) {
					s.append(", ");
				}
			}
			s.append("</html>");
			panel.getExplanation().setText(s.toString());
		}
	}

	private static Map<String, Set<XAttribute>> getTraceAttributeMap(XLog log) {
		Map<String, Set<XAttribute>> traceAttributes = new TreeMap<String, Set<XAttribute>>();

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
