package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HighlightingFilterTraceWithEventTwice extends HighlightingFilter {

	MultiAttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Event happening twice filter";
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

	public boolean countInColouring(IvMTrace trace) {
		String key = panel.getSelectedKey();
		int count = 0;
		for (IvMMove move : trace) {
			if (move.getAttributes() != null && move.getAttributes().containsKey(key)
					&& panel.getSelectedAttributes().contains(move.getAttributes().get(key).toString())) {
				count++;
				if (count >= 2) {
					return true;
				}
			}
		}
		return false;
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

}
