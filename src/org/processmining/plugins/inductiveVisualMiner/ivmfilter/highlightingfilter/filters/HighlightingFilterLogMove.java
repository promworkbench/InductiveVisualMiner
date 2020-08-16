package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class HighlightingFilterLogMove extends HighlightingFilterEvent {

	@Override
	public String getName() {
		return "Log move filter";
	}

	@Override
	public boolean staysInLog(IvMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			for (IvMMove event : trace) {
				if (event.isLogMove()) {
					String value = attribute.getLiteral(event);
					if (value != null && panel.getSelectedLiterals().contains(value)) {
						return true;
					}
				}
			}
		} else if (attribute.isNumeric()) {
			for (IvMMove event : trace) {
				if (event.isLogMove()) {
					double value = attribute.getNumeric(event);
					if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
							&& value <= panel.getSelectedNumericMax()) {
						return true;
					}
				}
			}
		} else if (attribute.isTime()) {
			for (IvMMove event : trace) {
				if (event.isLogMove()) {
					long value = attribute.getTime(event);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
							&& value <= panel.getSelectedTimeMax()) {
						return true;
					}
				}
			}
		} else if (attribute.isDuration()) {
			for (IvMMove event : trace) {
				if (event.isLogMove()) {
					long value = attribute.getDuration(event);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
							&& value <= panel.getSelectedTimeMax()) {
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
					.setText("Include only traces that have at least one log move having an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least one log move " + panel.getExplanation() + ".");
		}
	}
}
