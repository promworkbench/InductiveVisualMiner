package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PreMiningFilterTraceWithEvent extends PreMiningTraceFilter {

	protected AttributeFilterGui panel = null;

	@Override
	public String getName() {
		return "Trace with event filter";
	}

	@Override
	public IvMFilterGui createGui(IvMDecoratorI decorator) {
		panel = new AttributeFilterGui(getName(), new Runnable() {
			public void run() {
				update();
				updateExplanation();
			}
		}, decorator);

		return panel;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo) {
		panel.setAttributes(attributesInfo.getEventAttributes());
		updateExplanation();
	}

	@Override
	public boolean staysInLog(IMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			for (XEvent event : trace) {
				String value = attribute.getLiteral(event);
				if (value != null && panel.getSelectedLiterals().contains(value)) {
					return true;
				}
			}
		} else if (attribute.isNumeric()) {
			for (XEvent event : trace) {
				double value = attribute.getNumeric(event);
				if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
						&& value <= panel.getSelectedNumericMax()) {
					return true;
				}
			}
		} else if (attribute.isTime()) {
			for (XEvent event : trace) {
				long value = attribute.getTime(event);
				if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
						&& value <= panel.getSelectedTimeMax()) {
					return true;
				}
			}
		} else if (attribute.isDuration()) {
			for (XEvent event : trace) {
				long value = attribute.getDuration(event);
				if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
						&& value <= panel.getSelectedTimeMax()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		return panel.isFiltering();
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least one event having an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least one event " + panel.getExplanation() + ".");
		}
	}
}