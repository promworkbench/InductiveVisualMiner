package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningEventFilter;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PreMiningFilterEvent extends PreMiningEventFilter {

	AttributeFilterGui panel = null;

	@Override
	public String getName() {
		return "Event filter";
	}

	@Override
	public IvMFilterGui createGui(IvMDecoratorI decorator) throws Exception {
		panel = new AttributeFilterGui(getName(), new Runnable() {
			public void run() {
				update();
				updateExplanation();
			}
		}, decorator);

		return panel;
	}

	@Override
	public boolean staysInLog(XEvent event) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			String value = attribute.getLiteral(event);
			if (value != null && panel.getSelectedLiterals().contains(value)) {
				return true;
			}
		} else if (attribute.isNumeric()) {
			double value = attribute.getNumeric(event);
			if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
					&& value <= panel.getSelectedNumericMax()) {
				return true;
			}
		} else if (attribute.isTime()) {
			long value = attribute.getTime(event);
			if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
				return true;
			}
		} else if (attribute.isDuration()) {
			long value = attribute.getDuration(event);
			if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo) {
		panel.setAttributes(attributesInfo.getEventAttributes());
		updateExplanation();
	}

	@Override
	protected boolean isEnabled() {
		return panel.isFiltering();
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel().setText("Include only events having an attribute as selected.");
		} else {
			panel.getExplanationLabel().setText("Include only events " + panel.getExplanation() + ".");
		}
	}

}
