package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

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
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.MultiAttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningEventFilter;

public class PreMiningFilterEvents extends PreMiningEventFilter {

	MultiAttributeFilterGui panel = null;
	boolean block = true;

	public boolean staysInLog(XEvent event) {
		String key = panel.getSelectedKey();
		if (event.getAttributes() != null && event.getAttributes().containsKey(key)
				&& panel.getSelectedAttributes().contains(event.getAttributes().get(key).toString())) {
			return true;
		}
		return false;
	}

	public String getName() {
		return "Event filter";
	}

	public IvMFilterGui createGui(XLog log, final AttributesInfo attributesInfo) {
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

	protected boolean isEnabled() {
		return !panel.getSelectedAttributes().isEmpty();
	}

	public void updateExplanation() {
		if (panel.getSelectedAttributes().isEmpty()) {
			panel.getExplanation().setText("<html>Include only events having an attribute as selected.</html>");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("<html>Include only events having attribute `");
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
			s.append("</html>");
			panel.getExplanation().setText(s.toString());
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
