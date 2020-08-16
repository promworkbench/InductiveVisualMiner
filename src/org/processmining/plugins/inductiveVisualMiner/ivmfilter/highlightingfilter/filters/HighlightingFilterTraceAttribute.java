package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class HighlightingFilterTraceAttribute extends HighlightingFilterEvent {

	@Override
	public IvMFilterGui createGui() {
		panel = new AttributeFilterGui(getName(), new Runnable() {
			public void run() {
				updateExplanation();
				update();
			}
		});
		return panel;
	}

	@Override
	public void setAttributesInfo(IvMAttributesInfo attributesInfo) {
		panel.setAttributes(attributesInfo.getTraceAttributes());
		updateExplanation();
	}

	@Override
	public String getName() {
		return "Trace attribute filter";
	}

	@Override
	public boolean staysInLog(IvMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			String value = attribute.getLiteral(trace);
			if (value != null && panel.getSelectedLiterals().contains(value)) {
				return true;
			}
		} else if (attribute.isNumeric()) {
			double value = attribute.getNumeric(trace);
			if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
					&& value <= panel.getSelectedNumericMax()) {
				return true;
			}
		} else if (attribute.isTime()) {
			long value = attribute.getTime(trace);
			if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
				return true;
			}
		} else if (attribute.isDuration()) {
			long value = attribute.getDuration(trace);
			if (value != Long.MIN_VALUE && value >= panel.getSelectedDurationMin()
					&& value <= panel.getSelectedDurationMax()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel().setText("Include only traces having an attribute as selected.");
		} else {
			panel.getExplanationLabel().setText("Include only traces " + panel.getExplanation() + ".");
		}
	}
}
