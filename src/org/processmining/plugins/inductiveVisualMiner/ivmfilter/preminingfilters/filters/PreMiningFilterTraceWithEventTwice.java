package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class PreMiningFilterTraceWithEventTwice extends PreMiningFilterTraceWithEvent {

	@Override
	public String getName() {
		return "Trace with event happening twice filter";
	}

	@Override
	public boolean staysInLog(IMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		int count = 0;
		if (attribute.isLiteral()) {
			for (XEvent event : trace) {
				String value = attribute.getLiteral(event);
				if (value != null && panel.getSelectedLiterals().contains(value)) {
					count++;
					if (count >= 2) {
						return true;
					}
				}
			}
		} else if (attribute.isNumeric()) {
			for (XEvent event : trace) {
				double value = attribute.getNumeric(event);
				if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
						&& value <= panel.getSelectedNumericMax()) {
					count++;
					if (count >= 2) {
						return true;
					}
				}
			}
		} else if (attribute.isTime()) {
			for (XEvent event : trace) {
				long value = attribute.getTime(event);
				if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
						&& value <= panel.getSelectedTimeMax()) {
					count++;
					if (count >= 2) {
						return true;
					}
				}
			}
		} else if (attribute.isDuration()) {
			for (XEvent event : trace) {
				long value = attribute.getDuration(event);
				if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
						&& value <= panel.getSelectedTimeMax()) {
					count++;
					if (count >= 2) {
						return true;
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
