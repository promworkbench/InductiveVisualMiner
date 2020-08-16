package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PreMiningFilterTrace extends PreMiningFilterTraceWithEvent {

	@Override
	public String getName() {
		return "Trace filter";
	}

	@Override
	public IvMFilterGui createGui() {
		panel = new AttributeFilterGui(getName(), new Runnable() {
			public void run() {
				update();
				updateExplanation();
			}
		});

		return panel;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo) {
		panel.setAttributes(attributesInfo.getTraceAttributes());
		updateExplanation();
	}

	@Override
	public boolean staysInLog(IMTrace trace) {
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
			if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
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
