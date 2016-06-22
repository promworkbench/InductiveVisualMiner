package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

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
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class MultiEventAttributeFilter extends HighlightingFilter {

	MultiAttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Event filter";
	}

	public IvMFilterGui createGui(final AttributesInfo attributesInfo) {
		panel = new MultiAttributeFilterGui(attributesInfo.getEventAttributesMap(), getName());

		// Key selector
		panel.getKeySelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				block = true;
				String selectedKey = panel.getSelectedKey();

				panel.replaceAttributes(attributesInfo.getEventAttributesMap().get(selectedKey));
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

	protected boolean fillGuiWithLog(IMLog log, IvMLog ivmLog) throws Exception {
		return false;
	}

	public boolean countInColouring(IvMTrace trace) {
		String key = panel.getSelectedKey();
		for (IvMMove event : trace) {
			if (event.getAttributes() != null && event.getAttributes().containsKey(key)
					&& panel.getSelectedAttributes().contains(event.getAttributes().get(key).toString())) {
				return true;
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return !panel.getSelectedAttributes().isEmpty();
	}

	public void updateExplanation() {
		if (panel.getSelectedAttributes().isEmpty()) {
			panel.getExplanation().setText(
					"<html>Include only traces that have at least one event having an attribute as selected.</html>");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("<html>Include only traces that have at least one event ");
			printKey(s);
			s.append("</html>");
			panel.getExplanation().setText(s.toString());
		}
	}

	public void printKey(StringBuilder s) {
		s.append("having attribute `");
		s.append(panel.getSelectedKey());
		s.append("' being ");
		List<String> attributes = panel.getSelectedAttributes();
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
	}

	public static Map<String, Set<XAttribute>> getEventAttributeMap(XLog log) {
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
