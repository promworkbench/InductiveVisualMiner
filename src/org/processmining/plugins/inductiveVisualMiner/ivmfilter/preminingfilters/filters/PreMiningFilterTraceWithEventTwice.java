package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;

public class PreMiningFilterTraceWithEventTwice extends PreMiningFilterTraceWithEvent {

	@Override
	public String getName() {
		return "Trace with event happening twice filter";
	}

	@Override
	public boolean staysInLog(IMTrace trace) {
		//TODO: update
		Attribute attribute = panel.getSelectedAttribute();
		int count = 0;
		if (attribute.isLiteral()) {
			for (XEvent event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName()) && panel
						.getSelectedLiterals().contains(event.getAttributes().get(attribute.getName()).toString())) {
					count++;
					if (count >= 2) {
						return true;
					}
				}
			}
		} else if (attribute.isNumeric()) {
			for (XEvent event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					double value = AttributeUtils.parseDoubleFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
						count++;
						if (count >= 2) {
							return true;
						}
					}
				}
			}
		} else if (attribute.isTime()) {
			for (XEvent event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					long value = AttributeUtils.parseTimeFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
						count++;
						if (count >= 2) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least two events having an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least two events " + panel.getExplanation() + ".");
		}
	}

}
