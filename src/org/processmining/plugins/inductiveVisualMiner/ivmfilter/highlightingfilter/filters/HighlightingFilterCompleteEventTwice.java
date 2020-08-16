package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class HighlightingFilterCompleteEventTwice extends HighlightingFilterEvent {

	@Override
	public String getName() {
		return "Complete event happening twice filter";
	}

	@Override
	public boolean staysInLog(IvMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		int count = 0;
		if (attribute.isLiteral()) {
			for (IvMMove event : trace) {
				if (event.isComplete()) {
					String value = attribute.getLiteral(event);
					if (value != null && panel.getSelectedLiterals().contains(value)) {
						count++;
						if (count >= 2) {
							return true;
						}
					}
				}
			}
		} else if (attribute.isNumeric()) {
			for (IvMMove event : trace) {
				if (event.isComplete()) {
					double value = attribute.getNumeric(event);
					if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
							&& value <= panel.getSelectedNumericMax()) {
						count++;
						if (count >= 2) {
							return true;
						}
					}
				}
			}
		} else if (attribute.isTime()) {
			for (IvMMove event : trace) {
				if (event.isComplete()) {
					long value = attribute.getTime(event);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
							&& value <= panel.getSelectedTimeMax()) {
						count++;
						if (count >= 2) {
							return true;
						}
					}
				}
			}
		} else if (attribute.isDuration()) {
			for (IvMMove event : trace) {
				if (event.isComplete()) {
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
		}
		return false;
	}

	@Override
	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel().setText(
					"Include only traces that have at least two completion events having an attribute as selected.");
		} else {
			panel.getExplanationLabel().setText(
					"Include only traces that have at least two completion events " + panel.getExplanation() + ".");
		}
	}

}
