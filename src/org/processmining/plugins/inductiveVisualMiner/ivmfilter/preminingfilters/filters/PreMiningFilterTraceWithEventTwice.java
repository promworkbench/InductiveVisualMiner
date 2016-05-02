package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.MultiAttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.MultiEventAttributeFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;

public class PreMiningFilterTraceWithEventTwice extends PreMiningTraceFilter {

	MultiAttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Trace with event happening twice filter";
	}

	public IvMFilterGui createGui(XLog log) {
		final Map<String, Set<XAttribute>> traceAttributes = MultiEventAttributeFilter.getEventAttributeMap(log);
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

	protected boolean isEnabled() {
		return !panel.getSelectedAttributes().isEmpty();
	}

	public void updateExplanation() {
		if (panel.getSelectedAttributes().isEmpty()) {
			panel.getExplanation().setText(
					"<html>Include only traces that have at least two events having an attribute as selected.</html>");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("<html>Include only traces that have at least two events having attribute `");
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

	public boolean staysInLog(IMTrace trace) {
		String key = panel.getSelectedKey();
		int count = 0;
		for (XEvent event : trace) {
			if (event.getAttributes() != null && event.getAttributes().containsKey(key)
					&& panel.getSelectedAttributes().contains(event.getAttributes().get(key))) {
				count++;
				if (count >= 2) {
					return true;
				}
			}
		}
		return false;
	}

}